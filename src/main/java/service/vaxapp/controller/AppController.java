package service.vaxapp.controller;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.vaxapp.UserSession;
import service.vaxapp.model.*;
import service.vaxapp.repository.*;
import service.vaxapp.service.ChatBotService;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Math.round;

@Controller
public class AppController {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private ForumAnswerRepository forumAnswerRepository;
    @Autowired
    private ForumQuestionRepository forumQuestionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VaccineCentreRepository vaccineCentreRepository;
    @Autowired
    private VaccineRepository vaccineRepository;
    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserSession userSession;
    @Autowired
    private ChatBotService chatBotService;

    @GetMapping("/")
    public String index(Model model) {
        ArrayList<AppointmentSlot> appSlots = (ArrayList<AppointmentSlot>) appointmentSlotRepository.findAll();

        // sort time slots by center and date
        Collections.sort(appSlots, new Comparator<AppointmentSlot>() {
            public int compare(AppointmentSlot o1, AppointmentSlot o2) {
                if (o1.getVaccineCentre().getName() == o2.getVaccineCentre().getName()) {
                    if (o1.getDate() == o2.getDate())
                        return o1.getStartTime().compareTo(o2.getStartTime());
                    return o1.getDate().compareTo(o2.getDate());
                }

                return o1.getVaccineCentre().getName().compareTo(o2.getVaccineCentre().getName());
            }
        });

        // only appointment slots 3 weeks after most recent appointment
        if(userSession.isLoggedIn()) {
            List<Appointment> apps = appointmentRepository.findDone(userSession.getUserId());
            if(!apps.isEmpty()) {
                apps.sort(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTime).reversed());

                // Get the most recent appointment
                Appointment mostRecentAppointment = apps.get(0);
                LocalDate mostRecentAppointmentDate = mostRecentAppointment.getDate();
                LocalTime mostRecentAppointmentTime = mostRecentAppointment.getTime();

                // Calculate the cutoff (three weeks after the most recent appointment)
                LocalDate cutoffDate = mostRecentAppointmentDate.plusDays(21);
                LocalTime cutoffTime = mostRecentAppointmentTime.plusMinutes(30);

                // Remove all appointment slots within three weeks following the most recent appointment
                appSlots.removeIf(slot -> slot.getDate().isBefore(cutoffDate) ||
                        (slot.getDate().isEqual(cutoffDate) && slot.getStartTime().isBefore(cutoffTime)));
            }
        }

        model.addAttribute("appSlots", appSlots);
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getNotifications());
        return "index";
    }

    @PostMapping(value = "/make-appointment")
    public String makeAppointment(@RequestParam Map<String, String> body, Model model,
            RedirectAttributes redirectAttributes) {
        if (!userSession.isLoggedIn()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to make an appointment.");
            return "redirect:/login";
        }

        // A user shouldn't have more than one pending appointment
        if (appointmentRepository.findPending(userSession.getUserId()) != null) {
            redirectAttributes.addFlashAttribute("error",
                    "You can only have one pending appointment at a time. Please check your appointment list.");
            return "redirect:/";
        }

        Integer centerId = Integer.valueOf(body.get("center_id"));
        LocalDate date = LocalDate.parse(body.get("date"), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalTime time = LocalTime.parse(body.get("time"));

        AppointmentSlot appSlot = appointmentSlotRepository.findByDetails(centerId, date, time);
        if (appSlot == null) {
            redirectAttributes.addFlashAttribute("error", "The appointment slot you selected is no longer available.");
            return "redirect:/";
        } else {
            List<Appointment> apps = appointmentRepository.findDone(userSession.getUserId());
            if(!apps.isEmpty()) {
                apps.sort(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTime).reversed());

                // Get the most recent appointment
                Appointment mostRecentAppointment = apps.get(0);
                LocalDate mostRecentAppointmentDate = mostRecentAppointment.getDate();
                LocalTime mostRecentAppointmentTime = mostRecentAppointment.getTime();

                // Calculate the cutoff (three weeks after the most recent appointment)
                LocalDate cutoffDate = mostRecentAppointmentDate.plusDays(21);
                LocalTime cutoffTime = mostRecentAppointmentTime.plusMinutes(30);

                if(appSlot.getDate().isBefore(cutoffDate) ||
                        (appSlot.getDate().isEqual(cutoffDate) && appSlot.getStartTime().isBefore(cutoffTime))) {
                    redirectAttributes.addFlashAttribute("error",
                            "You cannot book an appointment within 3 weeks of your last vaccination.");
                    return "redirect:/";
                }
            }
        }

        Appointment app = new Appointment(appSlot.getVaccineCentre(), appSlot.getDate(), appSlot.getStartTime(),
                userSession.getUser(), "pending");
        appointmentRepository.save(app);
        appointmentSlotRepository.delete(appSlot);

        sendNotification(4, "/profile/"+userSession.getUserId(), userRepository.findByEmail("admin@vaxapp.com"));

        redirectAttributes.addFlashAttribute("success",
                "Your appointment has been made! Please see the details of your new appointment.");
        return "redirect:/profile";
    }

    @GetMapping("/stats")
    public String statistics(Model model) {
        //Retrieve all vaccine records
        Map<User, List<Vaccine>> vaccinationsByUser = vaccineRepository.findAll().stream().collect(Collectors.groupingBy(Vaccine::getUser, Collectors.toList()));
        getStats(model, vaccinationsByUser);
        return "stats.html";
    }

    @PostMapping("/stats")
    public String statistics(Model model, @RequestParam("nationality") List<String> nationalities) {
        if(nationalities.isEmpty()) {
            return statistics(model);
        }
        nationalities.replaceAll(String::toLowerCase);
        model.addAttribute("nationalities", nationalities);
        model.addAttribute("flagCodes", getFlagCodes(nationalities));
        Map<User, List<Vaccine>> vaccinationsByUser = vaccineRepository.findAll().stream().collect(Collectors.groupingBy(Vaccine::getUser, Collectors.toList()));
        // Filter vaccinations by selected nationalities
        vaccinationsByUser.entrySet().removeIf(entry -> !nationalities.contains(entry.getKey().getNationality().toLowerCase()));
        getStats(model, vaccinationsByUser);
        return "stats.html";
    }

    public void getStats(Model model, Map<User, List<Vaccine>> vaccinationsByUser) {
        List<Vaccine> vaccines = vaccinationsByUser.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        List<User> peopleVaccinated = new ArrayList<>(vaccinationsByUser.keySet());
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getNotifications());
        model.addAttribute("totalDoses", vaccines.size());
        model.addAttribute("totalPeople", peopleVaccinated.size());

        // Process and group vaccines by type (e.g. Pfizer), then collect statistics for each type
        Map<String, Map<String, Object>> statsByVaccineType = vaccines.stream().collect(Collectors.groupingBy(
                Vaccine::getType,
                Collectors.collectingAndThen(Collectors.toList(), vaccineList -> {
                    Map<String, Object> typeStats = new HashMap<>();
                    List<User> usersByType = vaccineList.stream().map(Vaccine::getUser).collect(Collectors.toList());
                    long totalByType = usersByType.size();
                    long maleByType = usersByType.stream().filter(user -> "male".equalsIgnoreCase(user.getGender())).count();
                    long femaleByType = totalByType - maleByType;

                    // Age distribution by decade (like in initial code, but here at the vaccine type level)
                    Map<Integer, Double> ageRangesByType = usersByType.stream()
                            .collect(Collectors.groupingBy(user -> (user.getAge() / 10) * 10,
                                    TreeMap::new, Collectors.collectingAndThen(Collectors.counting(), count -> count * 100.0 / totalByType)));

                    // Store statistics in the typeStats map
                    typeStats.put("total", totalByType);
                    typeStats.put("male", maleByType);
                    typeStats.put("female", femaleByType);
                    typeStats.put("ageRanges", ageRangesByType);
                    typeStats.put("maleDosePercent", maleByType * 100.0 / (double) totalByType);
                    typeStats.put("femaleDosePercent", femaleByType * 100.0 / (double) totalByType);
                    return typeStats;
                })
        ));
        // Add the collected statistics to the model attribute for the view
        model.addAttribute("statsByVaccineType", statsByVaccineType);

        // For a global view, return ALL nationality doses
        Map<String, Long> dosesByAllNationalities = vaccinationsByUser.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().getNationality(), // Group by nationality
                        Collectors.summingLong(entry -> entry.getValue().size()) // Sum the count of vaccines
                ));

        model.addAttribute("dosesByAllNationalities", dosesByAllNationalities);

        long total = peopleVaccinated.size();
        long male = peopleVaccinated.stream().filter(x -> x.getGender().equalsIgnoreCase("male")).count();
        long female = total - male;
        Map<Integer, Double> ageRanges = new TreeMap<>();

        for (AtomicInteger i = new AtomicInteger(1); i.get() <= 8; i.incrementAndGet()) {
            long count = peopleVaccinated.stream().filter(x -> x.getAge() / 10 == i.get()).count();
            ageRanges.put(i.get() * 10, count == 0 ? 0.0 : (count / (double) total) * 100);
        }

        model.addAttribute("agerange", ageRanges);
        model.addAttribute("maleDosePercent", round(male * 100.0 / (double) total));
        model.addAttribute("femaleDosePercent", round(female * 100.0 / (double) total));
    }

    /**
     * User Area
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getNotifications());
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("pps") String pps,
            RedirectAttributes redirectAttributes) {
        // make sure the user is found in db by PPS and email
        User user = userRepository.findByCredentials(email, pps);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Wrong credentials.");
            return "redirect:/login";
        }
        userSession.setUserId(user.getId());
        redirectAttributes.addFlashAttribute("success", "Welcome, " + user.getFullName() + "!");
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getNotifications());
        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String register(User user, RedirectAttributes redirectAttributes) {
        if (user.getDateOfBirth().isEmpty() || user.getEmail().isEmpty() || user.getAddress().isEmpty()
                || user.getFullName().isEmpty() || user.getGender().isEmpty() || user.getNationality().isEmpty()
                || user.getPhoneNumber().isEmpty() || user.getPPS().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "All fields are required!");
            return "redirect:/register";
        }
        if (userRepository.findByPPS(user.getPPS()) != null) {
            redirectAttributes.addFlashAttribute("error", "User with this PPS number already exists.");
            return "redirect:/register";
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            redirectAttributes.addFlashAttribute("error", "User with this email already exists.");
            return "redirect:/register";
        }
        // Ensure user is 18 or older
        if (isUserUnderage(user.getDateOfBirth())) {
            redirectAttributes.addFlashAttribute("error", "Users under 18 cannot create an account.");
            return "redirect:/register";
        }
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Account created! You can sign in now.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout() {
        userSession.setUserId(null);
        return "redirect:/";
    }

    @GetMapping("/forum")
    public String forum(Model model) {
        // Retrieve all questions and answers from database
        List<ForumQuestion> questions = forumQuestionRepository.findAll();
        model.addAttribute("questions", questions);
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getNotifications());
        return "forum";
    }

    @GetMapping("/ask-a-question")
    public String askAQuestion(Model model, RedirectAttributes redirectAttributes) {
        // If not logged in or admin, return to forum
        if (!userSession.isLoggedIn() || userSession.getUser().isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "Users must be logged in to ask questions.");
            return "redirect:/forum";
        }
        // If user, return ask-a-question page
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getNotifications());
        return "ask-a-question";
    }

    @PostMapping("/ask-a-question")
    public String askAQuestion(@RequestParam String title, @RequestParam String details, Model model,
            RedirectAttributes redirectAttributes) {
        // If user is not logged in or is admin
        if (!userSession.isLoggedIn() || userSession.getUser().isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "Users must be logged in to ask questions.");
            return "redirect:/forum";
        }

        // Create new question entry in db
        ForumQuestion newQuestion = new ForumQuestion(title, details, getDateSubmitted(), userSession.getUser());

        // Add question to database
        forumQuestionRepository.save(newQuestion);

        redirectAttributes.addFlashAttribute("success", "The question was successfully submitted.");

        sendNotification(3, "/question?id=" + newQuestion.getId(), userRepository.findByEmail("admin@vaxapp.com"));
        // Redirect to new question page
        return "redirect:/question?id=" + newQuestion.getId();
    }

    @PostMapping("/question")
    public String answerQuestion(@RequestParam String body, @RequestParam String id, Model model,
            RedirectAttributes redirectAttributes) {
        // Retrieving question
        try {
            Integer questionId = Integer.parseInt(id);
            Optional<ForumQuestion> question = forumQuestionRepository.findById(questionId);
            if (question.isPresent()) {
                // If user is admin
                if (userSession.isLoggedIn() && userSession.getUser() != null && userSession.getUser().isAdmin()) {
                    // Create new answer entry in db
                    ForumAnswer newAnswer = new ForumAnswer(body, getDateSubmitted());
                    // Save forum question and answer
                    newAnswer.setAdmin(userSession.getUser());
                    newAnswer.setQuestion(question.get());
                    forumAnswerRepository.save(newAnswer);
                    question.get().addAnswer(newAnswer);
                    forumQuestionRepository.save(question.get());

                    sendNotification(2, "/question?id="+question.get().getId(), question.get().getUser());

                    redirectAttributes.addFlashAttribute("success", "The answer was successfully submitted.");
                    // Redirect to updated question page
                    return "redirect:/question?id=" + question.get().getId();
                } else {
                    redirectAttributes.addFlashAttribute("error",
                            "Only admins may answer questions. If you are an admin, please log in.");
                    // Redirect to unchanged same question page
                    return "redirect:/question?id=" + question.get().getId();
                }
            }

        } catch (NumberFormatException e) {
            return "redirect:/forum";
        }
        return "redirect:/forum";
    }

    @GetMapping("/profile")
    public String profile(Model model, RedirectAttributes redirectAttributes) {
        if (!userSession.isLoggedIn()) {
            redirectAttributes.addFlashAttribute("error",
                    "You must be logged in to view your profile. If you do not already have an account, please register.");
            return "redirect:/login";
        }

        List<Appointment> apps = appointmentRepository.findByUser(userSession.getUserId());
        Collections.reverse(apps);

        List<Vaccine> vaxes = vaccineRepository.findByUser(userSession.getUserId());
        Collections.reverse(vaxes);
        readNotification("/profile");

        model.addAttribute("vaccineCenters", vaccineCentreRepository.findAll());
        model.addAttribute("appointments", apps);
        model.addAttribute("vaccines", vaxes);
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getNotifications());
        model.addAttribute("userProfile", userSession.getUser());
        model.addAttribute("isSelf", true);
        model.addAttribute("userDoses", vaxes.size());
        model.addAttribute("userQuestions", forumQuestionRepository.findByUser(userSession.getUserId()).size());
        model.addAttribute("userAppts", appointmentRepository.findByUser(userSession.getUserId()).size());
        return "profile";
    }

    @GetMapping("/profile/{stringId}")
    public String profile(@PathVariable String stringId, Model model) {
        if (stringId == null)
            return "404";

        try {
            Integer id = Integer.valueOf(stringId);
            Optional<User> user = userRepository.findById(id);

            if (!user.isPresent()) {
                return "404";
            }

            List<Vaccine> vaxes = vaccineRepository.findByUser(user.get().getId());

            if (userSession.isLoggedIn() && userSession.getUser().isAdmin()) {
                // admins can see everybody's appointments
                List<Appointment> apps = appointmentRepository.findByUser(user.get().getId());
                Collections.reverse(apps);
                Collections.reverse(vaxes);

                model.addAttribute("appointments", apps);
                model.addAttribute("vaccines", vaxes);
            }

            readNotification("/profile/"+stringId);

            model.addAttribute("vaccineCenters", vaccineCentreRepository.findAll());
            model.addAttribute("userSession", userSession);
            model.addAttribute("notifications", getNotifications());
            model.addAttribute("userProfile", user.get());
            model.addAttribute("userQuestions", forumQuestionRepository.findByUser(user.get().getId()).size());
            model.addAttribute("userDoses", vaxes.size());
            model.addAttribute("userAppts", appointmentRepository.findByUser(user.get().getId()).size());
            return "profile";
        } catch (NumberFormatException ex) {
            return "404";
        }
    }

    @GetMapping("/cancel-appointment/{stringId}")
    public String cancelAppointment(@PathVariable String stringId, RedirectAttributes redirectAttributes) {
        if (!userSession.isLoggedIn())
            return "redirect:/login";

        Integer id = Integer.valueOf(stringId);
        Appointment app = appointmentRepository.findById(id).get();

        if (!userSession.getUser().isAdmin() && userSession.getUser().getId() != app.getUser().getId()) {
            // Hacker detected! You can't cancel someone else's appointment!
            return "404";
        }

        app.setStatus("cancelled");
        appointmentRepository.save(app);

        AppointmentSlot appSlot = new AppointmentSlot(app.getVaccineCentre(), app.getDate(), app.getTime());
        appointmentSlotRepository.save(appSlot);

        redirectAttributes.addFlashAttribute("success", "The appointment was successfully cancelled.");

        if (app.getUser().getId() != userSession.getUser().getId()) {
            return "redirect:/profile/" + app.getUser().getId();
        }

        return "redirect:/profile";
    }

    @GetMapping("/question")
    public String getQuestionById(@RequestParam(name = "id") Integer id, Model model,
            RedirectAttributes redirectAttributes) {
        // Retrieve question
        Optional<ForumQuestion> question = forumQuestionRepository.findById(id);
        if (question.isPresent()) {
            // Return question information
            model.addAttribute("question", question.get());
            model.addAttribute("userSession", userSession);
            readNotification("/question?id="+id);
            model.addAttribute("notifications", getNotifications());
            return "question.html";
        } else {
            redirectAttributes.addFlashAttribute("error", "The question you requested could not be found.");
            // Redirect if question not found
            return "redirect:/forum";
        }
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        if (!userSession.isLoggedIn())
            return "redirect:/login";

        List<Vaccine> vaxes = vaccineRepository.findByUser(userSession.getUserId());
        Collections.reverse(vaxes);
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getAllNotifications());
        model.addAttribute("userProfile", userSession.getUser());
        model.addAttribute("isSelf", true);
        model.addAttribute("userDoses", vaxes.size());
        model.addAttribute("userQuestions", forumQuestionRepository.findByUser(userSession.getUserId()).size());
        model.addAttribute("userAppts", appointmentRepository.findByUser(userSession.getUserId()).size());
        return "notifications";
    }

    /**
     * Admin area
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        if (!userSession.isLoggedIn() || !userSession.getUser().isAdmin())
            return "redirect:/login";

        List<User> users = userRepository.findAll();
        List<User> usersPending = new ArrayList<>();
        users.forEach(u -> {
            if (appointmentRepository.findPending(u.getId()) != null) {
                usersPending.add(u);
            }
        });

        model.addAttribute("usersPending", usersPending);
        model.addAttribute("users", users);
        model.addAttribute("userSession", userSession);
        model.addAttribute("notifications", getNotifications());
        return "dashboard";
    }

    @PostMapping(value = "/find-user")
    public String findUser(@RequestParam Map<String, String> body, Model model) {
        String input = body.get("input");

        User user = userRepository.findByPPSorName(input);
        if (user == null) {
            return "redirect:/dashboard";
        }
        return "redirect:/profile/" + user.getId();
    }

    @PostMapping(value = "/assign-vaccine")
    public String assignVaccine(@RequestParam Map<String, String> body, Model model,
            RedirectAttributes redirectAttributes) {
        if (!userSession.isLoggedIn() || !userSession.getUser().isAdmin()) {
            return "redirect:/login";
        }

        LocalDate vaxDate = LocalDate.parse(body.get("date"), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Integer userId = Integer.valueOf(body.get("user_id"));
        Integer centreId = Integer.valueOf(body.get("center_id"));
        String vaxType = body.get("vaccine");

        User vaxUser = userRepository.findById(userId).get();
        VaccineCentre vaxCentre = vaccineCentreRepository.findById(centreId).get();
        redirectAttributes.addFlashAttribute("success", "The vaccine was recorded.");

        // See how many other doses there are per user
        List<Vaccine> vaccines = vaccineRepository.findByUser(userId);
        if (vaccines == null || vaccines.size() == 0) {
            // Getting date in 3 weeks for second vaccination between 9 and 17
            LocalDate date = vaxDate.plusDays(21);
            LocalTime time = LocalTime.of(9, 00, 00);
            Appointment appointment = appointmentRepository.findByDetails(centreId, date, time);
            while (appointment != null) {
                time = time.plusMinutes(15);
                if (time.getHour() > 17) {
                    if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
                        date = date.plusDays(3);
                    } else {
                        date = date.plusDays(1);
                    }
                    time = LocalTime.of(9, 00, 00);
                }
                appointment = appointmentRepository.findByDetails(centreId, date, time);
            }
            User user = userRepository.findById(userId).get();
            // Creating new appointment for the user
            appointment = new Appointment(vaxCentre, date, time, user, "pending");
            appointmentRepository.save(appointment);
            redirectAttributes.addFlashAttribute("success",
                    "The vaccine was recorded and a new appointment at least 3 weeks from now has been made for the user.");
        }
        // Save new vaccine
        Vaccine vax = new Vaccine(userSession.getUser(), vaxDate, vaxCentre, vaxUser, vaxType);
        vaccineRepository.save(vax);

        return "redirect:/profile/" + userId;
    }

    @GetMapping("/complete-appointment/{stringId}")
    public String completeAppointment(@PathVariable String stringId, RedirectAttributes redirectAttributes) {
        if (!userSession.isLoggedIn())
            return "redirect:/login";

        if (!userSession.getUser().isAdmin()) {
            // Hacker detected! You can't modify if you're not an admin!
            return "404";
        }

        Integer id = Integer.valueOf(stringId);
        Appointment app = appointmentRepository.findById(id).get();

        app.setStatus("done");
        appointmentRepository.save(app);

        sendNotification(1, "/profile", app.getUser());

        redirectAttributes.addFlashAttribute("success", "The appointment was marked as complete.");

        if (app.getUser().getId() != userSession.getUser().getId()) {
            return "redirect:/profile/" + app.getUser().getId();
        }

        return "redirect:/profile";
    }

    @GetMapping("/chat")
    @ResponseBody
    public String chat(@RequestParam String prompt) {
        try {
            return chatBotService.chatToBot(prompt);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * /########################
     * <p>
     * Helpers
     * </p>
     * /#######################
     */
    private List<Notification> getNotifications() {
        if(userSession.isLoggedIn()) {
            return notificationRepository.findUnread(userSession.getUserId())
                    .stream()
                    .sorted(Comparator.comparing(Notification::getDate)
                            .thenComparing(Notification::getTime)
                            .reversed())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<Notification> getAllNotifications() {
        if(userSession.isLoggedIn()) {
            return notificationRepository.findUnread(userSession.getUserId())
                    .stream()
                    .sorted(Comparator.comparing(Notification::isRead)
                            .thenComparing(Notification::getDate).reversed()
                            .thenComparing(Notification::getTime).reversed())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void readNotification(String url) {
        if(userSession.isLoggedIn()) {
            List<Notification> notifications = notificationRepository.findAll(userSession.getUserId());
            for (Notification notification : notifications) {
                if (notification.getUrl().equalsIgnoreCase(url)) {
                    notification.setRead(true);
                }
            }
            notificationRepository.saveAll(notifications);
        }
    }

    private void sendNotification(int type, String url, User user) {
        String[] messages = {
                "Your appointment has been completed.","Your question has been answered.",
                "A new question has been posted.","A new appointment is pending."
        };

        if(type > 0 && type <= messages.length && userRepository.existsById(user.getId())) {
            notificationRepository.save(new Notification(messages[type-1], url, type, LocalDate.now(), LocalTime.now(), user));
        }
    }

    private String getDateSubmitted() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return currentDate.format(formatter);
    }

    private boolean isUserUnderage(String dateOfBirth) {
        LocalDate dob = LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return Period.between(dob, LocalDate.now()).getYears() < 18;
    }

    private List<String> getFlagCodes(List<String> nationalities) {
        Map<String, String> countryFlagMap = new HashMap<>();
        countryFlagMap.put("afghan", "af");
        countryFlagMap.put("albanian", "al");
        countryFlagMap.put("algerian", "dz");
        countryFlagMap.put("american", "us");
        countryFlagMap.put("andorran", "ad");
        countryFlagMap.put("angolan", "ao");
        countryFlagMap.put("antiguans", "ag");
        countryFlagMap.put("argentinean", "ar");
        countryFlagMap.put("armenian", "am");
        countryFlagMap.put("australian", "au");
        countryFlagMap.put("austrian", "at");
        countryFlagMap.put("azerbaijani", "az");
        countryFlagMap.put("bahamian", "bs");
        countryFlagMap.put("bahraini", "bh");
        countryFlagMap.put("bangladeshi", "bd");
        countryFlagMap.put("barbadian", "bb");
        countryFlagMap.put("barbudans", "ag");
        countryFlagMap.put("batswana", "bw");
        countryFlagMap.put("belarusian", "by");
        countryFlagMap.put("belgian", "be");
        countryFlagMap.put("belizean", "bz");
        countryFlagMap.put("beninese", "bj");
        countryFlagMap.put("bhutanese", "bt");
        countryFlagMap.put("bolivian", "bo");
        countryFlagMap.put("bosnian", "ba");
        countryFlagMap.put("brazilian", "br");
        countryFlagMap.put("british", "gb");
        countryFlagMap.put("bruneian", "bn");
        countryFlagMap.put("bulgarian", "bg");
        countryFlagMap.put("burkinabe", "bf");
        countryFlagMap.put("burmese", "mm");
        countryFlagMap.put("burundian", "bi");
        countryFlagMap.put("cambodian", "kh");
        countryFlagMap.put("cameroonian", "cm");
        countryFlagMap.put("canadian", "ca");
        countryFlagMap.put("cape verdean", "cv");
        countryFlagMap.put("central african", "cf");
        countryFlagMap.put("chadian", "td");
        countryFlagMap.put("chilean", "cl");
        countryFlagMap.put("chinese", "cn");
        countryFlagMap.put("colombian", "co");
        countryFlagMap.put("comoran", "km");
        countryFlagMap.put("congolese", "cg");
        countryFlagMap.put("costa rican", "cr");
        countryFlagMap.put("croatian", "hr");
        countryFlagMap.put("cuban", "cu");
        countryFlagMap.put("cypriot", "cy");
        countryFlagMap.put("czech", "cz");
        countryFlagMap.put("danish", "dk");
        countryFlagMap.put("djibouti", "dj");
        countryFlagMap.put("dominican", "do");
        countryFlagMap.put("dutch", "nl");
        countryFlagMap.put("east timorese", "tl");
        countryFlagMap.put("ecuadorean", "ec");
        countryFlagMap.put("egyptian", "eg");
        countryFlagMap.put("emirian", "ae");
        countryFlagMap.put("equatorial guinean", "gq");
        countryFlagMap.put("eritrean", "er");
        countryFlagMap.put("estonian", "ee");
        countryFlagMap.put("ethiopian", "et");
        countryFlagMap.put("fijian", "fj");
        countryFlagMap.put("filipino", "ph");
        countryFlagMap.put("finnish", "fi");
        countryFlagMap.put("french", "fr");
        countryFlagMap.put("gabonese", "ga");
        countryFlagMap.put("gambian", "gm");
        countryFlagMap.put("georgian", "ge");
        countryFlagMap.put("german", "de");
        countryFlagMap.put("ghanaian", "gh");
        countryFlagMap.put("greek", "gr");
        countryFlagMap.put("grenadian", "gd");
        countryFlagMap.put("guatemalan", "gt");
        countryFlagMap.put("guinea-bissauan", "gw");
        countryFlagMap.put("guinean", "gn");
        countryFlagMap.put("guyanese", "gy");
        countryFlagMap.put("haitian", "ht");
        countryFlagMap.put("herzegovinian", "ba");
        countryFlagMap.put("honduran", "hn");
        countryFlagMap.put("hungarian", "hu");
        countryFlagMap.put("icelander", "is");
        countryFlagMap.put("indian", "in");
        countryFlagMap.put("indonesian", "id");
        countryFlagMap.put("iranian", "ir");
        countryFlagMap.put("iraqi", "iq");
        countryFlagMap.put("irish", "ie");
        countryFlagMap.put("israeli", "il");
        countryFlagMap.put("italian", "it");
        countryFlagMap.put("ivorian", "ci");
        countryFlagMap.put("jamaican", "jm");
        countryFlagMap.put("japanese", "jp");
        countryFlagMap.put("jordanian", "jo");
        countryFlagMap.put("kazakhstani", "kz");
        countryFlagMap.put("kenyan", "ke");
        countryFlagMap.put("kittian and nevisian", "kn");
        countryFlagMap.put("kuwaiti", "kw");
        countryFlagMap.put("kyrgyz", "kg");
        countryFlagMap.put("laotian", "la");
        countryFlagMap.put("latvian", "lv");
        countryFlagMap.put("lebanese", "lb");
        countryFlagMap.put("liberian", "lr");
        countryFlagMap.put("libyan", "ly");
        countryFlagMap.put("liechtensteiner", "li");
        countryFlagMap.put("lithuanian", "lt");
        countryFlagMap.put("luxembourger", "lu");
        countryFlagMap.put("macedonian", "mk");
        countryFlagMap.put("malagasy", "mg");
        countryFlagMap.put("malawian", "mw");
        countryFlagMap.put("malaysian", "my");
        countryFlagMap.put("maldivan", "mv");
        countryFlagMap.put("malian", "ml");
        countryFlagMap.put("maltese", "mt");
        countryFlagMap.put("marshallese", "mh");
        countryFlagMap.put("mauritanian", "mr");
        countryFlagMap.put("mauritian", "mu");
        countryFlagMap.put("mexican", "mx");
        countryFlagMap.put("micronesian", "fm");
        countryFlagMap.put("moldovan", "md");
        countryFlagMap.put("monacan", "mc");
        countryFlagMap.put("mongolian", "mn");
        countryFlagMap.put("moroccan", "ma");
        countryFlagMap.put("mosotho", "ls");
        countryFlagMap.put("motswana", "bw");
        countryFlagMap.put("mozambican", "mz");
        countryFlagMap.put("namibian", "na");
        countryFlagMap.put("nauruan", "nr");
        countryFlagMap.put("nepalese", "np");
        countryFlagMap.put("new zealander", "nz");
        countryFlagMap.put("ni-vanuatu", "vu");
        countryFlagMap.put("nicaraguan", "ni");
        countryFlagMap.put("nigerien", "ne");
        countryFlagMap.put("north korean", "kp");
        countryFlagMap.put("northern irish", "gb-nir");
        countryFlagMap.put("norwegian", "no");
        countryFlagMap.put("omani", "om");
        countryFlagMap.put("pakistani", "pk");
        countryFlagMap.put("palauan", "pw");
        countryFlagMap.put("panamanian", "pa");
        countryFlagMap.put("papua new guinean", "pg");
        countryFlagMap.put("paraguayan", "py");
        countryFlagMap.put("peruvian", "pe");
        countryFlagMap.put("polish", "pl");
        countryFlagMap.put("portuguese", "pt");
        countryFlagMap.put("qatari", "qa");
        countryFlagMap.put("romanian", "ro");
        countryFlagMap.put("russian", "ru");
        countryFlagMap.put("rwandan", "rw");
        countryFlagMap.put("saint lucian", "lc");
        countryFlagMap.put("salvadoran", "sv");
        countryFlagMap.put("samoan", "ws");
        countryFlagMap.put("san marinese", "sm");
        countryFlagMap.put("sao tomean", "st");
        countryFlagMap.put("saudi", "sa");
        countryFlagMap.put("scottish", "gb-sct");
        countryFlagMap.put("senegalese", "sn");
        countryFlagMap.put("serbian", "rs");
        countryFlagMap.put("seychellois", "sc");
        countryFlagMap.put("sierra leonean", "sl");
        countryFlagMap.put("singaporean", "sg");
        countryFlagMap.put("slovakian", "sk");
        countryFlagMap.put("slovenian", "si");
        countryFlagMap.put("solomon islander", "sb");
        countryFlagMap.put("somali", "so");
        countryFlagMap.put("south african", "za");
        countryFlagMap.put("south korean", "kr");
        countryFlagMap.put("spanish", "es");
        countryFlagMap.put("sri lankan", "lk");
        countryFlagMap.put("sudanese", "sd");
        countryFlagMap.put("surinamer", "sr");
        countryFlagMap.put("swazi", "sz");
        countryFlagMap.put("swedish", "se");
        countryFlagMap.put("swiss", "ch");
        countryFlagMap.put("syrian", "sy");
        countryFlagMap.put("taiwanese", "tw");
        countryFlagMap.put("tajik", "tj");
        countryFlagMap.put("tanzanian", "tz");
        countryFlagMap.put("thai", "th");
        countryFlagMap.put("togolese", "tg");
        countryFlagMap.put("tongan", "to");
        countryFlagMap.put("trinidadian or tobagonian", "tt");
        countryFlagMap.put("tunisian", "tn");
        countryFlagMap.put("turkish", "tr");
        countryFlagMap.put("tuvaluan", "tv");
        countryFlagMap.put("ugandan", "ug");
        countryFlagMap.put("ukrainian", "ua");
        countryFlagMap.put("uruguayan", "uy");
        countryFlagMap.put("uzbekistani", "uz");
        countryFlagMap.put("venezuelan", "ve");
        countryFlagMap.put("vietnamese", "vn");
        countryFlagMap.put("welsh", "gb-wls");
        countryFlagMap.put("yemenite", "ye");
        countryFlagMap.put("zambian", "zm");
        countryFlagMap.put("zimbabwean", "zw");

        List<String> flags = new ArrayList<>();
        nationalities.forEach(e -> flags.add(countryFlagMap.getOrDefault(e, "")));
        return flags;
    }
}
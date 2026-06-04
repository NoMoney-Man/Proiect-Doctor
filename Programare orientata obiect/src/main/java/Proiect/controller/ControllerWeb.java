package Proiect.controller;

import Proiect.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ControllerWeb {

    private final ProgramariService programariService;

    public ControllerWeb(ProgramariService programariService) {
        this.programariService = programariService;
    }

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/")
    public String login(@RequestParam String email,
                        @RequestParam String parola,
                        HttpSession session,
                        Model model) {

        Utilizator u = new Utilizator().login(email, parola);

        if (u == null) {
            model.addAttribute("eroare", "Email sau parola incorecta");
            return "login";
        }

        session.setAttribute("utilizator", u);

        if (u instanceof Pacient) return "redirect:/pacient";
        return "redirect:/medic";
    }

    @GetMapping("/register-pacient")
    public String registerPacientPage() {
        return "register";
    }

    @PostMapping("/register-pacient")
    public String registerPacient(@RequestParam String nume,
                                  @RequestParam String prenume,
                                  @RequestParam String email,
                                  @RequestParam String nrTelefon,
                                  @RequestParam String dataNasterii,
                                  @RequestParam String gen,
                                  @RequestParam String parola,
                                  Model model) {

        Pacient p = new Pacient();

        boolean ok = p.registerPacient(nume, prenume, email, nrTelefon, dataNasterii, gen, parola);

        if (!ok) {
            model.addAttribute("eroare", "Email deja folosit");
            return "register";
        }

        return "redirect:/";
    }

    @GetMapping("/register-medic")
    public String registerMedicPage() {
        return "register-medic";
    }

    @PostMapping("/register-medic")
    public String registerMedic(@RequestParam String nume,
                                @RequestParam String prenume,
                                @RequestParam String email,
                                @RequestParam String nrTelefon,
                                @RequestParam String dataNasterii,
                                @RequestParam String gen,
                                @RequestParam String parola,
                                @RequestParam String specializare,
                                Model model) {

        Medic m = new Medic();

        boolean ok = m.registerMedic(nume, prenume, email, nrTelefon, dataNasterii, gen, parola, specializare);

        if (!ok) {
            model.addAttribute("eroare", "Eroare la inregistrare");
            return "register-medic";
        }

        return "redirect:/";
    }

    @GetMapping("/pacient")
    public String paginaPacient(HttpSession session, Model model,
                                @RequestParam(required = false) String specializare,
                                @RequestParam(required = false) String data) {

        programariService.actualizeazaProgramariFinalizate();

        Utilizator u = (Utilizator) session.getAttribute("utilizator");

        if (u == null || !(u instanceof Pacient)) {
            return "redirect:/";
        }

        model.addAttribute("user", u);
        model.addAttribute("programari", programariService.getProgramari(u));
        model.addAttribute("pService", programariService);

        if (specializare != null && !specializare.isEmpty() && data != null && !data.isEmpty()) {
            boolean[] oreOcupate = programariService.getOreOcupate(specializare, data);
            model.addAttribute("oreOcupate", oreOcupate);
            model.addAttribute("specializareSelectata", specializare);
            model.addAttribute("dataSelectata", data);
        }

        return "pacient";
    }

    @GetMapping("/medic")
    public String paginaMedic(HttpSession session, Model model) {

        Utilizator u = (Utilizator) session.getAttribute("utilizator");

        if (u == null || !(u instanceof Medic)) {
            return "redirect:/";
        }

        model.addAttribute("user", u);
        model.addAttribute("programari", programariService.getProgramari(u));

        return "medic";
    }

    @PostMapping("/salveaza-programare")
    public String salveazaProgramare(@RequestParam String specializare,
                                     @RequestParam String data,
                                     @RequestParam int schimb,
                                     HttpSession session,
                                     Model model) {

        Utilizator u = (Utilizator) session.getAttribute("utilizator");

        if (u == null || !(u instanceof Pacient)) {
            return "redirect:/";
        }

        try {
            programariService.creeazaProgramare(u, specializare, data, schimb);
        } catch (RuntimeException e) {
            model.addAttribute("eroare", e.getMessage());
            return paginaPacient(session, model, specializare, data);
        }

        return "redirect:/pacient";
    }

    @PostMapping("/confirma-programare")
    public String confirma(@RequestParam int id, HttpSession session) {
        Utilizator u = (Utilizator) session.getAttribute("utilizator");
        if (u == null || !(u instanceof Medic)) {
            return "redirect:/";
        }
        programariService.confirma(id);
        return "redirect:/medic";
    }

    @PostMapping("/anuleaza-programare")
    public String anuleaza(@RequestParam int id, HttpSession session) {
        Utilizator u = (Utilizator) session.getAttribute("utilizator");
        if (u == null || !(u instanceof Medic)) {
            return "redirect:/";
        }
        programariService.anuleaza(id);
        return "redirect:/medic";
    }

    @GetMapping("/istoric-consultatii")
    public String istoricConsultatii(Model model, HttpSession session) {

        Utilizator u = (Utilizator) session.getAttribute("utilizator");

        if (u == null) {
            return "redirect:/";
        }

        model.addAttribute("user", u);
        model.addAttribute("consultatii", programariService.getIstoricProgramari(u));

        return "istoric-consultatii";
    }

    @GetMapping("/informatii")
    public String infoPage(Model model, HttpSession session) {

        Utilizator u = (Utilizator) session.getAttribute("utilizator");

        if (u == null) {
            return "redirect:/";
        }

        model.addAttribute("user", u);
        return "informatii";
    }

    @GetMapping("/lista-medici")
    public String listaMediciPage(Model model, HttpSession session) {

        Utilizator u = (Utilizator) session.getAttribute("utilizator");

        if (u == null) {
            return "redirect:/";
        }

        Medic m = new Medic();
        model.addAttribute("listaMedici", m.listaMedici());
        model.addAttribute("user", u);

        return "lista-medici";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
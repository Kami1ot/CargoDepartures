package com.example.cargodeps7.Controllers;

import com.example.cargodeps7.Models.Cargo;
import com.example.cargodeps7.Repository.CargoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class CargoController {

    private final CargoRepository cargoRepository;

    public CargoController(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Главная Страница");
        return "greeting";
    }
    @GetMapping("/support")
    public String support() {
        return "support";
    }

    @GetMapping("/Cargo")
    public String cargos(Model model) {
        Iterable<Cargo> cargos = cargoRepository.findAll();
        model.addAttribute("title", "Страница грузов");
        model.addAttribute("Cargos", cargos);
        return "Plays"; // Используем оригинальное имя HTML-шаблона
    }

    @GetMapping("/addCargo")
    public String addCargo(Model model) {
        model.addAttribute("title", "Страница добавления груза");
        return "addPlay"; // Используем оригинальное имя HTML-шаблона
    }

    @GetMapping("/Cargo/{id}")
    public String updateCargo(@PathVariable long id, Model model) {
        if (!cargoRepository.existsById(id)) {
            return "redirect:/Cargo";
        }
        Optional<Cargo> cargo = cargoRepository.findById(id);
        ArrayList<Cargo> res = new ArrayList<>();
        cargo.ifPresent(res::add);
        model.addAttribute("Cargo", res);
        model.addAttribute("title", "Страница редактирования");
        return "PlayDetails"; // Используем оригинальное имя HTML-шаблона
    }

    @GetMapping("/Cargo/filter")
    public String searchCargos(
            @RequestParam(required = false) String cargoName,
            @RequestParam(required = false) String departureCity,
            @RequestParam(required = false) String arrivalCity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "asc") String sort,
            Model model) {

        Sort.Direction sortDirection = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy = Sort.by(sortDirection, "departureDate");

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<Cargo> cargos;

        if (cargoName != null || departureCity != null || arrivalCity != null || startDate != null || endDate != null) {
            cargos = cargoRepository.findByParams(cargoName, departureCity, arrivalCity, startDateTime, endDateTime, sortBy);
        } else {
            cargos = cargoRepository.findAll(sortBy);
        }

        model.addAttribute("Cargos", cargos);
        return "Plays"; // Используем оригинальное имя HTML-шаблона
    }

    @GetMapping("/Cargo/stats")
    public String stats(Model model) {
        List<Object[]> stats = cargoRepository.findCargoStats();

        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (Object[] row : stats) {
            dates.add(row[0].toString());
            counts.add((Long) row[1]);
        }

        model.addAttribute("dates", dates);
        model.addAttribute("counts", counts);
        return "Play_stats"; // Используем оригинальное имя HTML-шаблона
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/addCargo")
    public String addCargo(
            @RequestParam String cargoName,
            @RequestParam String content,
            @RequestParam String departureCity,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime departureDate,
            @RequestParam String arrivalCity,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime arrivalDate,
            Model model) {
        Cargo cargo = new Cargo(cargoName, content, departureCity, departureDate, arrivalCity, arrivalDate);
        cargoRepository.save(cargo);
        return "redirect:/Cargo";
    }

    @PostMapping("/Cargo/save")
    public String saveCargo(
            @RequestParam("id") long id,
            @RequestParam String cargoName,
            @RequestParam String content,
            @RequestParam String departureCity,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime departureDate,
            @RequestParam String arrivalCity,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime arrivalDate,
            Model model) {
        Cargo cargo = cargoRepository.findById(id).orElseThrow();
        cargo.setCargoName(cargoName);
        cargo.setContent(content);
        cargo.setDepartureCity(departureCity);
        cargo.setDepartureDate(departureDate);
        cargo.setArrivalCity(arrivalCity);
        cargo.setArrivalDate(arrivalDate);
        cargoRepository.save(cargo);
        return "redirect:/Cargo";
    }

    @PostMapping("/Cargo/{id}/remove")
    public String removeCargo(@PathVariable long id, Model model) {
        Cargo cargo = cargoRepository.findById(id).orElseThrow();
        cargoRepository.delete(cargo);
        return "redirect:/Cargo";
    }
}

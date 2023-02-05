package com.example.foodservicev1.controller;

import com.example.foodservicev1.entity.*;
import com.example.foodservicev1.service.CustomerService;
import com.example.foodservicev1.service.FoodService;
import com.example.foodservicev1.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestMapping("")
@SessionAttributes({"saveResponse", "updateResponse", "deleteResponse", "loginResponse", "email"})
@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private FoodService foodService;

    /////////////////////////// Homepage //////////////////////////////
    @GetMapping("/api/customer")
    public ModelAndView home(Model model) {
        // Set some value of attributes for modal appear
        model.addAttribute("saveResponse", -2);
        model.addAttribute("updateResponse", -2);
        model.addAttribute("deleteResponse", -2);
        model.addAttribute("loginResponse", -2);

        // Exit to login page if not login yet
        if (model.getAttribute("email") == null) {
            return new ModelAndView("redirect:/api/customer/login");
        }

        // Add restaurants
        model.addAttribute("restaurants", restaurantService.findAll());

        // Create the page
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/home");

        // Return home page
        return modelAndView;
    }


    /////////////////////////// Read //////////////////////////////
    // Page of Login
    @GetMapping("/api/customer/login")
    public String loginPage(Model model) {
        String modalContent = "Login successfully";
        String modalId = "modal";

        int response = -2;

        if (model.getAttribute("loginResponse") == null) {
            modalId = "notModal";
        } else {
            response = (int) model.getAttribute("loginResponse");
            if (response == -1) {
                modalContent = "The email does not exist!";
            } else if (response == 0) {
                modalContent = "Wrong password!";
            }
            model.addAttribute("modalId", modalId);
            model.addAttribute("modalContent", modalContent);
        }

        model.addAttribute("loginResponse", null);

        return "customer/login";
    }

    @GetMapping("/api/customer/restaurant/{username}")
    public String restaurantPage(Model model, @PathVariable String username) {
        model.addAttribute("restaurant", restaurantService.findByUsername(username));
        model.addAttribute("foods", foodService.findByRestaurantUsername(username));

        return "customer/restaurant";
    }




    /*@PostMapping("order")
    public String hello(@RequestParam List<String> idList, @RequestParam List<String> quantityList) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));



        System.out.println(idList);
        System.out.println(quantityList);
        return "Hi";
    }*/


    /////////////////////////// Create //////////////////////////////
    @PostMapping("/api/customer/order")
    public String restaurantPage(Model model,
                                 @RequestParam List<String> idList,
                                 @RequestParam List<String> nameList,
                                 @RequestParam List<Double> priceList,
                                 @RequestParam List<Integer> quantityList,
                                 @RequestParam String restaurantUsername) {




        LocalDateTime now = LocalDateTime.now();
        String uniqueID = UUID.randomUUID().toString();

        List<Food> foods = new ArrayList<>();
        List<OrderFood> orderFoods = new ArrayList<>();


        model.addAttribute("email", "theboost1305@gmail.com");

        Order order = new Order(uniqueID, restaurantUsername, (String)model.getAttribute("email"), now);




        for (int i=0; i<idList.size(); i++) {
            /*foods.add(new Food(idList.get(i), "luckrestaurant",
                    nameList.get(i), priceList.get(i), quantityList.get(i), ""));*/
            orderFoods.add(new OrderFood(order.getId(), idList.get(i), quantityList.get(i)));
        }

        String username = "luckrestaurant";
        model.addAttribute("restaurant", restaurantService.findByUsername(username));
        model.addAttribute("foods", foodService.findByRestaurantUsername(username));

        System.out.println(order);
        System.out.println(orderFoods);

        

        return "customer/restaurant";
    }


    /////////////////////////// Update //////////////////////////////


    /////////////////////////// Delete //////////////////////////////


    /////////////////////////// Login //////////////////////////////
    @PostMapping("/api/customer/login")
    public ModelAndView login(Model model, @RequestParam String email, @RequestParam String password) {
        model.addAttribute("customer", new Customer());

        int response = customerService.login(email, password);
        model.addAttribute("loginResponse", response);

        if (response == 1) {
            model.addAttribute("email", email);
            return new ModelAndView("redirect:/api/customer");
        }
        return new ModelAndView("redirect:/api/customer/login");
    }


    /////////////////////////// Logout //////////////////////////////
    @GetMapping("/api/customer/logout")
    public ModelAndView logout(Model model, HttpSession httpsession, SessionStatus status) {
        status.setComplete();
        httpsession.invalidate();
        return new ModelAndView("redirect:/api/customer/login");
    }

}
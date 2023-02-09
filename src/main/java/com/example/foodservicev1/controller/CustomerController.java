package com.example.foodservicev1.controller;

import com.example.foodservicev1.dto.OrderDetailDto;
import com.example.foodservicev1.entity.*;
import com.example.foodservicev1.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@RequestMapping("")
@SessionAttributes({"saveResponse", "updateResponse", "deleteResponse", "loginResponse", "customerEmail"})
@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private OrderFoodService orderFoodService;

    @Autowired
    private FoodService foodService;

    @Autowired
    private OrderDetailService orderDetailService;

    /////////////////////////// Homepage //////////////////////////////
    @GetMapping("/api/customer")
    public ModelAndView home(Model model) {
        // Set some value of attributes for modal appear
        model.addAttribute("saveResponse", -2);
        model.addAttribute("updateResponse", -2);
        model.addAttribute("deleteResponse", -2);
        model.addAttribute("loginResponse", -2);

        // Exit to login page if not login yet
        if (model.getAttribute("customerEmail") == null) {
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

    // Page of Create Customers (Register)
    @GetMapping("/api/customer/register")
    public String saveCustomerPage(Model model) {
        // Add object for form
        model.addAttribute("customer", new Customer());

        // Set up the modal message
        String modalId = "modal";
        String modalContent = "Create Customer successfully";
        if (model.getAttribute("saveResponse") == null) {
            modalId = "notModal";
        } else {
            if ((int) model.getAttribute("saveResponse") == -2) {
                modalId = "notModal";
            } else if ((int) model.getAttribute("saveResponse") == 0) {
                modalContent = "The email is existed";
            }
        }

        model.addAttribute("modalId", modalId);
        model.addAttribute("modalContent", modalContent);

        // Return the page
        return "customer/save-customer";
    }


    // Page of Update Customer (Profile)
    @GetMapping("/api/customer/profile")
    public String updateCustomerPage(Model model) {
        model.addAttribute("customer", customerService.findByEmail((String) model.getAttribute("customerEmail")));
        String modalId = "modal";
        String modalContent = "Update Customer successfully";
        if ((int) model.getAttribute("updateResponse") == -2) {
            modalId = "notModal";
        } else if ((int) model.getAttribute("updateResponse") == 0) {
            modalContent = "Something wrong happen!";
        }
        model.addAttribute("modalId", modalId);
        model.addAttribute("modalContent", modalContent);
        return "customer/profile";
    }

    @GetMapping("/api/customer/orders")
    public String ordersPage(Model model) {
        model.addAttribute("orders", serviceOrderService.find());
        return "customer/orders";
    }

    @GetMapping("/api/customer/order-confirm/{orderId}")
    public String orderConfirmPage(Model model, @PathVariable String orderId) {
        List<OrderDetailDto> orderDetailDtos = orderDetailService.findByOrderId(orderId);
        model.addAttribute("orderID", orderId);
        model.addAttribute("orderDetailDtos", orderDetailDtos);
        return "customer/order-confirm";
    }

    @GetMapping("/api/customer/order/{orderId}")
    public String orderDetailPage(Model model, @PathVariable String orderId) {
        model.addAttribute("orderID", orderId);
        model.addAttribute("orderDetailDtos", orderDetailService.findByOrderId(orderId));
        return "customer/order-detail";
    }



    /////////////////////////// Create //////////////////////////////
    @PostMapping("/api/customer/customer")
    public ModelAndView saveCustomer(Model model, @ModelAttribute Customer customer) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("saveResponse", customerService.save(customer));
        return new ModelAndView("redirect:/api/customer/register");
    }

    @PostMapping("/api/customer/order")
    public ModelAndView addOrder(Model model,
                                 @RequestParam List<String> foodIdList,
                                 @RequestParam List<String> foodNameList,
                                 @RequestParam List<Integer> quantityList,
                                 @RequestParam List<Double> priceList,
                                 @RequestParam String restaurantUsername,
                                 @RequestParam String restaurantName) {

        String uniqueID = UUID.randomUUID().toString();

        serviceOrderService.save(new ServiceOrder(uniqueID, restaurantUsername, restaurantName,
                (String) model.getAttribute("customerEmail"), null));

        for (int i = 0; i < foodIdList.size(); i++) {
            orderFoodService.save(new OrderFood(uniqueID, foodIdList.get(i),
                    foodNameList.get(i), quantityList.get(i), priceList.get(i)));
        }

        model.addAttribute("orderId", uniqueID);

        return new ModelAndView("redirect:/api/customer/order-confirm/" + uniqueID);
    }


    /////////////////////////// Update //////////////////////////////
    @PutMapping("/api/customer/customer")
    public ModelAndView updateCustomer(Model model, @ModelAttribute Customer customer) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("updateResponse", customerService.update(customer));
        return new ModelAndView("redirect:/api/customer/profile");
    }


    /////////////////////////// Delete //////////////////////////////
    @DeleteMapping("api/customer/customer/{email}")
    public ModelAndView delete(Model model, @PathVariable String email, HttpSession httpsession, SessionStatus status) {
        model.addAttribute("admin", new Admin());
        model.addAttribute("deleteResponse", customerService.delete(email));
        status.setComplete();
        httpsession.invalidate();
        return new ModelAndView("redirect:/api/customer/login");
    }

    /////////////////////////// Login //////////////////////////////
    @PostMapping("/api/customer/login")
    public ModelAndView login(Model model, @RequestParam String email, @RequestParam String password) {
        model.addAttribute("customer", new Customer());

        int response = customerService.login(email, password);
        model.addAttribute("loginResponse", response);

        if (response == 1) {
            model.addAttribute("customerEmail", email);
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

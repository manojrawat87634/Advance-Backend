package com.example.demo.controllers.StudentController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repo.auth.UserRepo;
import com.example.demo.models.auth.UserModel;

@RestController
@RequestMapping("/admin")
public class StudentController {
    @Autowired
    UserRepo userRepo;
     @GetMapping("/get-all-users")
    public Page<UserModel> getUsers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        return userRepo.findAll(pageable);
    }    
}

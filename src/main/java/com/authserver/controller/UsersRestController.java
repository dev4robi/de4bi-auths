package com.authserver.controller;

import java.util.Map;

import com.authserver.data.vo.PostUsersVO;
import com.authserver.data.vo.PutUsersVO;
import com.authserver.service.UsersService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController("/users")
public class UsersRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UsersService usersSvc;
   
    @GetMapping
    public Map<String, Object> getUsers() {
        return null;
    }

    @PostMapping
    public Map<String, Object> postUsers(@RequestBody PostUsersVO postUsersVO) {
        return usersSvc.insertUser(postUsersVO.getEmail(), postUsersVO.getPassword(), postUsersVO.getNickname(),
                                   postUsersVO.getFullName(), postUsersVO.getGender(), postUsersVO.getDateOfBirth()).toMap();
    }

    @PutMapping
    public Map<String, Object> putUsers(@RequestBody PutUsersVO putUsersVO) {
        return usersSvc.updateUser(putUsersVO.getUserJwt(), putUsersVO.getPassword(), putUsersVO.getNickname(),
                                   putUsersVO.getFullName(), putUsersVO.getGender(), putUsersVO.getDateOfBirth()).toMap();
    }

    @DeleteMapping
    public Map<String, Object> deleteUsers() {
        return null;
    }
}
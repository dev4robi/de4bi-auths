package com.authserver.controller;

import java.util.Map;

import com.authserver.data.vo.PostUsersVO;
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
        logger.info("email: " + postUsersVO.getEmail() + ", password: " + postUsersVO.getPassword() +
                    ", nickname: " + postUsersVO.getNickname() + ", fullName" + postUsersVO.getFullName() +
                    ", gender: " + postUsersVO.getGender() + ", dateOfBirth: " + postUsersVO.getDateOfBirth());
        return usersSvc.insertUser(postUsersVO.getEmail(), postUsersVO.getPassword(), postUsersVO.getNickname(),
                                   postUsersVO.getFullName(), postUsersVO.getGender(), postUsersVO.getDateOfBirth()
                                  ).toMap();
    }

    @PutMapping
    public Map<String, Object> putUsers() {
        // 회원정보 수정부터 시작 @@
        return null;
    }

    @DeleteMapping
    public Map<String, Object> deleteUsers() {
        return null;
    }
}
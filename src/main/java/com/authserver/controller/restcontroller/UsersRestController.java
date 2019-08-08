package com.authserver.controller.restcontroller;

import java.util.Map;

import com.authserver.data.ApiResult;
import com.authserver.data.vo.PostUsersVO;
import com.authserver.data.vo.PutUsersVO;
import com.authserver.service.UsersService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
public class UsersRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UsersService usersSvc;
   
    @GetMapping("/users/{id}")
    public Map<String, Object> getUsersById(@RequestHeader String userJwt,
                                            @PathVariable Long id) {
        return usersSvc.selectUserByKey("id", id.toString()).toMap();
    }

    @GetMapping("/users/email/{email}")
    public Map<String, Object> getUsersByEmail(@RequestHeader String userJwt,
                                               @PathVariable String email) {
        return usersSvc.selectUserByKey("email", email).toMap();
    }

    @GetMapping("/users/nickname/{nickname}")
    public Map<String, Object> getUsersByNickName(@RequestHeader String userJwt,
                                                  @PathVariable String nickname) {
        return usersSvc.selectUserByKey("nickname", nickname).toMap();
    }

    @GetMapping("/users")
    public Map<String, Object> getUserFromJwt(@RequestHeader String userJwt) {
        return usersSvc.selectUserByJwt(userJwt).toMap();
    }

    @PostMapping("/users")
    public Map<String, Object> postUsers(@RequestBody PostUsersVO postUsersVO) {
        return usersSvc.insertUser(postUsersVO.getEmail(), postUsersVO.getPassword(), postUsersVO.getNickname(),
                                   postUsersVO.getFullName(), postUsersVO.getGender(), postUsersVO.getDateOfBirth()).toMap();
    }

    @PutMapping("/users")
    public Map<String, Object> putUsers(@RequestHeader String userJwt, @RequestBody PutUsersVO putUsersVO) {
        return usersSvc.updateUser(userJwt, putUsersVO.getPassword(), putUsersVO.getNickname(),
                                   putUsersVO.getFullName(), putUsersVO.getGender(), putUsersVO.getDateOfBirth()).toMap();
    }

    @DeleteMapping("/users")
    public Map<String, Object> deleteUsers(@RequestHeader String userJwt) {
        return usersSvc.deleteUser(userJwt).toMap();
    }
}
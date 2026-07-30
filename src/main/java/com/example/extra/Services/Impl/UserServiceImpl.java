package com.example.extra.Services.Impl;

import com.example.extra.DTO.UserDTO;
import com.example.extra.Entities.Location;
import com.example.extra.Entities.User;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Exceptions.Custom.NotFound;
import com.example.extra.Mappers.UserMapper;
import com.example.extra.Services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ImageServiceImpl imageService;
    private final LocationServiceImpl locationService;

    @Override
    public UserDTO getUserById(String id) {
        try {
           checkUserExistenceById(id);
           String image = imageService.getImageByIdentifier(id);
           UserDTO user = userMapper.getUserById(id);

           if (image != null) {
               user.setImageUrl(image);
           }

           Location location = locationService.getLocation(id);

           if (location != null) {
               user.setLocation(location);
           }

           return user;
        }catch (NotFound e) {
            log.error("User not found with id: {}", id);
            throw e;
        }
        catch (Exception e) {
            throw new InternalServerError("Unexpected error occurred while getting user by id: {}", e);
        }
    }

    @Override
    public User getUserByEmail(String email) {

            try {
                checkUserExistenceByEmail(email);
                return userMapper.getUserByEmail(email);
            }catch (NotFound e) {
                log.error("User not found with email: {}", email);
                throw e;
            }
            catch (Exception e) {
                throw new InternalServerError("Unexpected error occurred while getting user: {}", e);
            }

    }

    @Override
    public void checkUserExistenceById(String id) {

        boolean isUserExist = userMapper.checkUserExistenceById(id);
        if (!isUserExist) {
            throw new NotFound("User not found with id: " + id);
        }
    }

    @Override
    public void checkUserExistenceByEmail(String email) {
        boolean isUserExist = userMapper.checkUserExistenceByEmail(email);
        if (!isUserExist) {
            throw new NotFound("User not found with identifier: " + email);
        }
    }


}

package com.rafaelvieira.letmebuy.services;

import com.rafaelvieira.letmebuy.dto.RoleDTO;
import com.rafaelvieira.letmebuy.dto.UserDTO;
import com.rafaelvieira.letmebuy.dto.UserInsertDTO;
import com.rafaelvieira.letmebuy.dto.UserUpdateDTO;
import com.rafaelvieira.letmebuy.entities.Costumer;
import com.rafaelvieira.letmebuy.entities.Role;
import com.rafaelvieira.letmebuy.entities.User;
import com.rafaelvieira.letmebuy.repository.RoleRepository;
import com.rafaelvieira.letmebuy.repository.UserRepository;
import com.rafaelvieira.letmebuy.services.handlers.DataBaseException;
import com.rafaelvieira.letmebuy.services.handlers.ResourceNotFoundException;
import com.rafaelvieira.letmebuy.services.handlers.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private AuthService authService;

    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        Page<User> list = repository.findAll(pageable);
        return list.map(UserDTO::new);
    }

    public User find(Long id) {
        User user = authService.authenticated();
        if (user==null || !user.hasRole("ROLE_ADMIN") && !id.equals(user.getId())) {
            throw new UnauthorizedException("Acesso negado");
        }

        Optional<User> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(
                "Objeto não encontrado! Id: " + id + ", Tipo: " + User.class.getName()));
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        authService.validateSelfOrAdmin(id);
        Optional<User> obj = repository.findById(id);
        User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO save(UserInsertDTO dto) {
        User entity = new User();
        copyDtoToEntity(dto, entity);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = repository.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        try {
            User entity = repository.getOne(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new UserDTO(entity);
        }
        catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    public void delete(Long id) {
        try {
            repository.deleteById(id);
        }
        catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DataBaseException("Integrity violation");
        }
    }

    private void copyDtoToEntity(UserDTO dto, User entity) {
        entity.setEmail(dto.getEmail());
        entity.getRoles().clear();
        for (RoleDTO roleDto : dto.getRoles()) {
            Role role = roleRepo.getOne(roleDto.getId());
            entity.getRoles().add(role);
        }
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = repository.findByEmail(username);
//        if (user == null) {
//            logger.error("User not found: " + username);
//            throw new UsernameNotFoundException("Email not found");
//        }
//        logger.info("User found: " + username);
//        return user;
//    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email);
        if (user == null) {
            logger.error("Email not found: " + email);
            throw new UsernameNotFoundException("Email not found" + email);
        }
        return new User(user.getId(), user.getEmail(), user.getPassword());
    }

    public static User authenticated() {
        try {
            return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        catch (Exception e) {
            return null;
        }
    }
}

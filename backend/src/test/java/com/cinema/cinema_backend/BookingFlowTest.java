package com.cinema.cinema_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingFlowTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate db;
    @Autowired PasswordEncoder passwords;

    @Test
    void scheduleAndHoldSeatsThroughGuestLoginAndCheckout() throws Exception {
        String password = "Example123!";
        db.update("insert into users (firstName,lastName,email,password,userType,customerStatus,promotions) values (?,?,?,?,?,?,?)",
                "Test","Customer","guest@example.test",passwords.encode(password),"CUSTOMER","ACTIVE",false);
        db.update("insert into users (firstName,lastName,email,password,userType) values (?,?,?,?,?)",
                "Test","Admin","admin@example.test",passwords.encode(password),"ADMIN");
        MockHttpSession admin = new MockHttpSession();
        admin.setAttribute("userId", db.queryForObject("select userID from users where userType='ADMIN'", Long.class));
        String movie = """
                {"title":"Matinee","genre":"Drama","description":"A matinee.","status":"CURRENTLY_RUNNING"}
                """;
        mvc.perform(post("/api/movies/admin").session(admin).contentType("application/json").content(movie))
                .andExpect(status().isCreated());
        long movieId = db.queryForObject("select movie_id from movies where title='Matinee'", Long.class);
        String time = LocalDate.now().plusDays(2) + "T14:00";
        for (int room = 1; room <= 3; room++) {
            db.update("insert into showrooms (roomID,name,capacity) values (?,?,2)", room, "Room " + room);
            db.update("insert into seats (roomID,`row`,seatNumber) values (?,'A',1),(?,'A',2)", room, room);
            String schedule = """
                    {"movieId":%d,"roomId":%d,"dateTime":"%s"}
                    """.formatted(movieId,room,time);
            mvc.perform(post("/api/showtimes/admin").session(admin).contentType("application/json").content(schedule))
                    .andExpect(status().isCreated());
            mvc.perform(post("/api/showtimes/admin").session(admin).contentType("application/json").content(schedule))
                    .andExpect(status().isConflict());
        }
        mvc.perform(get("/api/showtimes").param("movieId", String.valueOf(movieId)))
                .andExpect(jsonPath("$.length()").value(3));
        long showId = db.queryForObject("select showID from shows where roomID=1", Long.class);
        long seat = db.queryForObject("select min(showSeatID) from show_seats where showID=?", Long.class, showId);
        String path = "/api/showtimes/" + showId;
        String selection = "{\"showSeatIds\":[" + seat + "]}";
        String checkout = "{\"showSeatIds\":[" + seat + "],\"ticketCount\":1}";
        MockHttpSession guest = new MockHttpSession();
        MockHttpSession other = new MockHttpSession();
        mvc.perform(put(path + "/selection").session(guest).contentType("application/json").content(selection))
                .andExpect(status().isNoContent());
        mvc.perform(put(path + "/selection").session(other).contentType("application/json").content(selection))
                .andExpect(status().isConflict());
        mvc.perform(get(path + "/seats").session(other)).andExpect(jsonPath("$[0].available").value(false));
        mvc.perform(post(path + "/reserve").session(guest).contentType("application/json").content(checkout))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").session(guest).contentType("application/json")
                .content("{\"email\":\"guest@example.test\",\"password\":\"Example123!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("guest@example.test"));
        mvc.perform(post(path + "/reserve").session(guest).contentType("application/json")
                .content(checkout.replace("Count\":1","Count\":2"))).andExpect(status().isBadRequest());
        mvc.perform(post(path + "/reserve").session(guest).contentType("application/json").content(checkout))
                .andExpect(status().isOk());
        mvc.perform(post(path + "/checkout-email").session(guest).contentType("application/json")
                .content("{\"email\":\"invalid\"}")).andExpect(status().isBadRequest());
        mvc.perform(post(path + "/checkout-email").session(guest).contentType("application/json")
                .content("{\"email\":\"alternate@example.test\"}")).andExpect(status().isNoContent())
                .andExpect(request().sessionAttribute("checkoutEmail", "alternate@example.test"));
        mvc.perform(post("/api/auth/logout").session(guest)).andExpect(status().isNoContent());
        mvc.perform(get(path + "/seats").session(other)).andExpect(jsonPath("$[0].available").value(true));
    }
}

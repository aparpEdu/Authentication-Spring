package com.example.googleauthenticationspring.google;

import com.example.googleauthenticationspring.exception.AuthenticationAPIException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Component
public class GoogleApi {
    public GoogleUserDTO fetchUserInfo(RestTemplate restTemplate, String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> peopleApiResponse = restTemplate.exchange(
                "https://people.googleapis.com/v1/people/me?personFields=birthdays,names,emailAddresses",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> peopleApiData = peopleApiResponse.getBody();
        List<Map<String, Object>> names = (List<Map<String, Object>>) peopleApiData.get("names");
        Map<String, Object> firstNameData = names.get(0);
        String firstName = (String) firstNameData.get("givenName");
        String lastName = (String) firstNameData.get("familyName");



        List<Map<String, Object>> emailAddresses = (List<Map<String, Object>>) peopleApiData.get("emailAddresses");
        Map<String, Object> emailAddressData = emailAddresses.get(0); // Assuming the first email is the primary email
        String emailAddress = (String) emailAddressData.get("value");



      return new GoogleUserDTO(emailAddress,firstName,lastName);
    }

    public void fetchUserBirthdate(RestTemplate restTemplate, String accessToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> peopleApiResponse = restTemplate.exchange(
                "https://people.googleapis.com/v1/people/me?personFields=birthdays",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> peopleApiData = peopleApiResponse.getBody();
        List<Map<String, Object>> birthdays = (List<Map<String, Object>>) peopleApiData.get("birthdays");
        if(birthdays == null){
            throw new AuthenticationAPIException(HttpStatus.BAD_REQUEST,"Birthdate is private");
        }
        Map<String, Object> birthdayData = birthdays.get(0);
        Map<String, Object> date = (Map<String, Object>) birthdayData.get("date");

        int year = (int) date.get("year");
        int month = (int) date.get("month");
        int day = (int) date.get("day");

        checkUserBirthday(year,month,day);
    }


    public void checkUserBirthday(int year, int month, int day){
        LocalDate birthdate = LocalDate.of(year,month,day);
        LocalDate currentDate = LocalDate.now();
        Period age = Period.between(birthdate, currentDate);
        if(age.getYears() < 18){
            throw new AuthenticationAPIException(HttpStatus.BAD_REQUEST,"User is underage");
        }
    }

}

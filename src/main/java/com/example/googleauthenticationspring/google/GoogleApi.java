package com.example.googleauthenticationspring.google;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
}

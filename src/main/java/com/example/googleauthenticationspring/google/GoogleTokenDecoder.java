//package com.example.googleauthenticationspring.google;
//
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.json.webtoken.JsonWebSignature;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class GoogleTokenDecoder {
//
//    public GoogleTokenDecoder() {
//    }
//
//    private  GoogleIdToken parseGoogleIdToken(String idTokenString) throws IOException {
//        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//
//        JsonWebSignature jws = JsonWebSignature.parser(jsonFactory)
//                .setPayloadClass(GoogleIdToken.Payload.class)
//                .parse(idTokenString);
//
//        return new GoogleIdToken(jws.getHeader(), (GoogleIdToken.Payload) jws.getPayload(), jws.getSignatureBytes(), jws.getSignedContentBytes());
//    }
//    public GoogleUserDTO decodeGoogleToken(String idToken) throws IOException {
//
//        GoogleIdToken googleIdToken = parseGoogleIdToken(idToken);
//
//        GoogleIdToken.Payload payload = googleIdToken.getPayload();
//
//
//        String email = payload.getEmail();
//        String givenName = (String) payload.get("given_name");
//        String familyName = (String) payload.get("family_name");
//
//        return new GoogleUserDTO(email, givenName, familyName);
//    }
//
//}

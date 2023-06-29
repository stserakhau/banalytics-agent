package com.banalytics.box.service.security;

//import org.springframework.context.event.EventListener;
//import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

//@RequiredArgsConstructor
//@Component
public class UserAuthenticationHandler {
//    private final UserProfileService userProfileService;

//    @EventListener
//    public void authenticationSuccessListen(AuthenticationSuccessEvent event) {
//        Object source = event.getSource();
//        if (source instanceof OAuth2LoginAuthenticationToken) {
//            OAuth2LoginAuthenticationToken t = (OAuth2LoginAuthenticationToken) source;
//            ClientRegistration cr = t.getClientRegistration();
//
//            Registrar registrar = Registrar.valueOf(
//                    cr.getRegistrationId()//google, facebook, etc
//            );
//            OAuth2User user = t.getPrincipal();
//            String email = registrar.extractEmail(user);
//            UserProfile userProfile = userProfileService.findByRegistrarEmail(registrar, email);
//            if (userProfile == null) {
//                userProfile = new UserProfile();
//                userProfile.setRegistrar(registrar);
//                registrar.fillUserProfile(user, userProfile);
//                userProfileService.saveOrUpdate(userProfile);
//            }
//        }
//    }
}

package com.be9expensphie.expensphie_backend.security;

import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.repository.HouseholdMemberRepository;
import com.be9expensphie.expensphie_backend.repository.UserRepository;
import com.be9expensphie.expensphie_backend.service.AppUserDetailsService;
import com.be9expensphie.expensphie_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;

@RequiredArgsConstructor
@Component
//channelInterceptor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final AppUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final HouseholdMemberRepository householdMemberRepository;


    //extract jwt token from frontend
    //this method run before message sent to the channel(message is the content)
    //mess includes headers+payload
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        //StompHeader allow to retrieve path/header/...
        StompHeaderAccessor accessor= MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if(accessor!=null&& StompCommand.CONNECT.equals((accessor.getCommand()))){
            String authHeader=accessor.getFirstNativeHeader("Authorization");
            //validate token
            if(authHeader!=null&&authHeader.startsWith("Bearer ")){
                //load user in4 to set in principal(same with context holder->save authen user in4)
                String token=authHeader.substring(7);
                String email=jwtUtil.extractUsername(token);
                UserDetails userDetails=userDetailsService.loadUserByUsername(email);

                //validate token
                if(!jwtUtil.validateToken(token,userDetails)){
                    throw new IllegalArgumentException("Invalid token");
                }

                //set user in principal
                Principal principal=new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                accessor.setUser(principal);
            }else{
                throw new IllegalArgumentException("token not valid");
            }
        }

        //check authorities when subscribe to a path
        if(accessor!=null&&StompCommand.SUBSCRIBE.equals(accessor.getCommand())){
            Principal principal=accessor.getUser();
            String destination =accessor.getDestination();

            if(destination==null||principal==null){
                throw new IllegalArgumentException("Unauthorized");
            }
            //check if user in household
            Long householdId=extractHouseholdId(destination);
            UserEntity user=userRepository.findByEmail(principal.getName())
                    .orElseThrow(()->new IllegalArgumentException("User not found"));

            boolean member=householdMemberRepository.findByUserAndHouseholdId(
                    user,householdId
            ).isPresent();

            if(!member){throw new IllegalArgumentException("Not Allowed");}
        }

        return message;
    }

    private Long extractHouseholdId(String destination){
        String[] parts=destination.split("/");
        if(parts.length<5){
            throw new IllegalArgumentException("Invalid destination");
        }
        return Long.valueOf(parts[3]);
    }
}

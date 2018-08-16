package com.example.authz.configurer;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableAuthorizationServer
public class AuthzServerConfig extends AuthorizationServerConfigurerAdapter {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
    security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.jdbc(dataSource).passwordEncoder(new BCryptPasswordEncoder());
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

    TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
    tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));

    endpoints
      .accessTokenConverter(accessTokenConverter())
      .tokenEnhancer(tokenEnhancerChain)
      .authorizationCodeServices(authorizationCodeServices())
      .approvalStore(approvalStore())
      .authenticationManager(authenticationManager);
  }
  
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {

    final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    KeyStoreKeyFactory keyStoreKeyFactory =
        new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), "password".toCharArray());
    converter.setKeyPair(keyStoreKeyFactory.getKeyPair("jwt"));
    return converter;
  }

  @Bean
  public TokenEnhancer tokenEnhancer() {
    return new CustomTokenEnhancer();
  }

  @Bean
  public ApprovalStore approvalStore() {
    return new JdbcApprovalStore(dataSource);
  }
  
  @Bean
  public AuthorizationCodeServices authorizationCodeServices() {
    return new JdbcAuthorizationCodeServices(dataSource);
  }

  public class CustomTokenEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
        OAuth2Authentication authentication) {

      DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(accessToken);

      // Add custome claim to JWT
      Map<String, Object> info =
          new LinkedHashMap<String, Object>(accessToken.getAdditionalInformation());
      info.put("customKey", "customValue");
      result.setAdditionalInformation(info);
      return result;
    }
  }
}

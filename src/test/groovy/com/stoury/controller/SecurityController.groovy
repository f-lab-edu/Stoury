package com.stoury.controller

import com.stoury.config.security.SecurityConfig
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.MemberService
import com.stoury.utils.JwtUtils
import org.junit.jupiter.api.extension.ExtendWith
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.request.FormParametersSnippet
import org.springframework.restdocs.request.QueryParametersSnippet
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.request.RequestPartsSnippet
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import spock.lang.Specification

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = SecurityConfig.class)
@MockBean([JpaMetamodelMappingContext.class, JwtUtils.class])
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class SecurityController extends Specification {
    @Autowired
    private WebApplicationContext context
    @SpringBean
    MemberService memberService = Mock()
    @SpringBean
    PasswordEncoder passwordEncoder = Mock()
    @Autowired
    private RestDocumentationContextProvider restDocumentation

    MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .alwaysDo(print())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build()
    }

    def documentWithParams(FormParametersSnippet snippet) {
        return document("{class-name}/" + specificationContext.currentIteration.name,
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                snippet)
    }

    def "Login"() {
        given:
        passwordEncoder.matches(_, _) >> true
        def parameters = RequestDocumentation.formParameters(
                RequestDocumentation.parameterWithName("email").description("Login email"),
                RequestDocumentation.parameterWithName("password").description("Login password"),
                RequestDocumentation.parameterWithName("latitude").description("Current location's latitude. Not required for user to input, nullable").optional(),
                RequestDocumentation.parameterWithName("longitude").description("Current location's longitude. Not required for user to input, nullable").optional(),
        )

        memberService.loadUserByUsername(_) >> new AuthenticatedMember(1, "test@email.com", "encrypted")
        expect:
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@email.com")
                .param("password", "notEncrypted"))
                .andExpect(status().isOk())
                .andDo(documentWithParams(parameters))
    }
}

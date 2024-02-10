package com.stoury.controller

import com.stoury.dto.member.AuthenticatedMember
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import spock.lang.Specification

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@ExtendWith(RestDocumentationExtension.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
abstract class AbstractRestDocsTests extends Specification {
    @Autowired
    private WebApplicationContext context

    @Autowired
    private RestDocumentationContextProvider restDocumentation

    MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(print())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build()
    }

    def document() {
        return document("{class-name}/" + specificationContext.currentIteration.name,
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()))
    }

    def documentWithPath(ParameterDescriptor parameterDescriptor) {
        return document("{class-name}/" + specificationContext.currentIteration.name,
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                pathParameters(parameterDescriptor))
    }

    def documentWithPathAndQuery(ParameterDescriptor parameterDescriptor, ParameterDescriptor queryDescriptor) {
        return document("{class-name}/" + specificationContext.currentIteration.name,
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                pathParameters(parameterDescriptor),
                queryParameters(queryDescriptor))
    }

    static RequestPostProcessor authenticatedMember(AuthenticatedMember member) {
        return (request) -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            return request;
        };
    }
}

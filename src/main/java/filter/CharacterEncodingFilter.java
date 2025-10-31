//package filter;
//
//import jakarta.servlet.Filter;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.FilterConfig;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.annotation.WebFilter;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//
//import java.io.IOException;
//
//@WebFilter("/*")
//public class CharacterEncodingFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response,
//                         FilterChain chain) throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        // Set UTF-8 encoding cho cả request và response
//        httpRequest.setCharacterEncoding("UTF-8");
//        httpResponse.setCharacterEncoding("UTF-8");
//        httpResponse.setContentType("text/html; charset=UTF-8");
//
//        System.out.println("DEBUG: CharacterEncodingFilter applied");
//        System.out.println("DEBUG: Original question: " + httpRequest.getParameter("question"));
//
//        chain.doFilter(httpRequest, httpResponse);
//    }
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {}
//
//    @Override
//    public void destroy() {}
//}
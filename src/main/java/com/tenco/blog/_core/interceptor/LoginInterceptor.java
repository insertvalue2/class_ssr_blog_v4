package com.tenco.blog._core.interceptor;

import com.tenco.blog._core.errors.exception.Exception401;
import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Component  // IoC 대상(싱글톤 패턴)
public class LoginInterceptor implements HandlerInterceptor {

    // preHandle 동작 흐름 (단 / 스프링부트 설정파일, 설정 클래스에 등록이 되어야 함 : 특정 URL)
    // 컨트롤러 들어 오기 전에 동작 하는 녀석
    // true --> 컨트롤러 안으로 들여 보낸다.
    // false --> 컨틀로러 안으로 못 들어감
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session =  request.getSession();
        User sessionUser =  (User)session.getAttribute("sessionUser");
        if(sessionUser == null) {
            throw new Exception401("로그인 먼저 해주세요");
        }
        return true;
    }

    // postHandle
    // 뷰가 렌더링 되기 바로전에 콜백 되는 메서드
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // TODO Auto-generated method stub
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    // afterCompletion
    // 요청 처리가 완료된 후, 즉 뷰가 완전 렌더링이 된 후에 호출 된다.
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

}

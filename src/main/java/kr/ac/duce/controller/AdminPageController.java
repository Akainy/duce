package kr.ac.duce.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import kr.ac.duce.handler.ErrorPageHandler;
import kr.ac.duce.model.*;
import kr.ac.duce.module.JSArrayParser;
import kr.ac.duce.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Controller
public class AdminPageController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AdminPasswordService AdminPasswordService;

    @Autowired
    MajorBranchSettingService MajorBranchService;

    @Autowired
    MemberManagementService MemberService;

    @Autowired
    SliderSettingService sliderSettingService;

    @Autowired
    IntroSettingService introSettingService;

    @GetMapping("/admin") // URL 주소
    public String adminPage(Model model) {
        return "redirect:/admin/slider"; // JSP 파일명
    } // 관리자 페이지뷰

    /*
        관리자 비밀번호 변경
     */

    @GetMapping("/admin/pwdchange") // 비밀번호 변경 테스트
    public String adminTest(Model model) {
        return "/admin/password-search"; // JSP 파일명
    }

    @PostMapping(value = "/admin/pwdchange.do", params = {"pwd", "changedpwd"})
    public String registerMember(@RequestParam(value = "pwd") String currentInputPwd, @RequestParam String changedpwd, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof MemberModel) {
            if (((MemberModel) principal).getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
                return "youAreNotAdmin";
            MemberModel user = (MemberModel) principal;
            String currentDBPwd = user.getPassword();
            if (passwordEncoder.matches(currentInputPwd, currentDBPwd)) {
                AdminPasswordService.changePassword(user.getId(), changedpwd);
                return "/admin/pwdchange"; // 비번 맞는다면 패스워드 변경
            } else {
                return ErrorPageHandler.throwError(model, "adminPassword", "/admin"); // 아니면 에러&이전 페이지
            }
        } else {
            return "/usererror"; // 이건 관리자 유저 권한조차 못찾는 경우
        }
    }

    /*
        전공/분야 설정 관련 기능
        POST: 추가
        PATCH: 변경
        DELETE: 삭제
     */
/*
    전공
 */
    @GetMapping("/admin/mb") // 전공/분야 설정 테스트
    public String majorTest(Model model) {
        List<BranchCodeModel> branchList = MajorBranchService.getBranch();
        List<MajorCodeModel> majorList = MajorBranchService.getMajor();
        model.addAttribute("majorList", majorList);
        model.addAttribute("branchList", branchList);
        return "/admin/major-branch-setting"; // JSP 파일명
    }

    @PostMapping("/admin/m/mod.do") // URL 주소
    public String majorAdd(Model model, @RequestParam String majorNo, @RequestParam String major) {
        try {
            MajorBranchService.addMajor(majorNo, major);
            return "redirect:/admin/mb";
        } catch (Exception e) {
            return "/error";
        }
    }

    @PatchMapping("/admin/m/mod.do") // URL 주소
    public String majorUpdate(Model model, @RequestParam("majorlist") String majorList) {
        try {
            JSArrayParser<MajorCodeModel> arrayParser = new JSArrayParser<MajorCodeModel>();
            Collection<MajorCodeModel> parsedMajorList = arrayParser.parse(majorList, MajorCodeModel.class);
            MajorBranchService.updateMajor(parsedMajorList);
            return "redirect:/admin/mb";
        } catch (Exception e) {
            throw e;
//            return "/error";
        }
    }

    @DeleteMapping("/admin/m/mod.do") // URL 주소
    public String majorDelete(Model model, @RequestParam("majorlist") String majorList) {
        JSArrayParser<String> arrayParser = new JSArrayParser<String>();
        Collection<String> parsedMajorList = arrayParser.parse(majorList, String.class);
        try {
            MajorBranchService.deleteMajor(parsedMajorList);
            return "redirect:/admin/mb";
        } catch (Exception ex) {
            if (ex instanceof DataIntegrityViolationException) {
                return "/error"; // 해당 코드를 사용하는 게시판이 존재한다는 에러
            }
            return "/"; // 그 외 에러
        }
    }

    /*
        분야
     */
    @PostMapping("/admin/b/mod.do") // URL 주소
    public String branchAdd(Model model, @RequestParam String branchNo, @RequestParam String branch) {
        try {
            MajorBranchService.addBranch(branchNo, branch);
            return "redirect:/admin/mb";
        } catch (Exception e) {
            return "/error";
        }
    }

    @PatchMapping("/admin/b/mod.do") // URL 주소
    public String branchUpdate(Model model, @RequestParam("branchlist") String branchList) {
        try {
            JSArrayParser<BranchCodeModel> arrayParser = new JSArrayParser<BranchCodeModel>();
            Collection<BranchCodeModel> parsedBranchList = arrayParser.parse(branchList, BranchCodeModel.class);
            MajorBranchService.updateBranch(parsedBranchList);
            return "redirect:/admin/mb";
        } catch (Exception e) {
            return "/error";
        }
    }

    @DeleteMapping("/admin/b/mod.do") // URL 주소
    public String branchDelete(Model model, @RequestParam("branchlist") String branchList) {
        JSArrayParser<String> arrayParser = new JSArrayParser<String>();
        Collection<String> parsedBranchList = arrayParser.parse(branchList, String.class);
        try {
            MajorBranchService.deleteBranch(parsedBranchList);
            return "redirect:/admin/mb";
        } catch (Exception ex) {
            if (ex instanceof DataIntegrityViolationException) {
                return "/error"; // 해당 코드를 사용하는 게시판이 존재한다는 에러
            }
            return "/"; // 그 외 에러
        }
    }

    /*
        회원 관리 기능
     */
    @GetMapping("/admin/member") // 회원관리 테스트
    public String memberTest(Model model, @RequestParam(value = "page", defaultValue = "1") int page) {
        if (page < 1) page = 1;
        int memberShowingCount = 6;
        int prevNextSplitter = 5;
        int allMemberCount = MemberService.getAllMemberCount();
        int allPage = (int) (Math.ceil(((double) allMemberCount) / memberShowingCount));
        int beginPage = (page - 2 > 1 ? page - 2 : 1);
        int endPage = (page + 2 > allPage ? allPage : page + 2);
        Collection<MemberModel> memberList = MemberService.getMembers(page, memberShowingCount);
        model.addAttribute("memberList", memberList);
        model.addAttribute("currentPage", page);
        model.addAttribute("beginPage", beginPage);
        model.addAttribute("endPage", endPage);
        return "/admin/member"; // JSP 파일명
    }

    @PatchMapping("/admin/member/mod.do") // URL 주소
    public String memberUpdate(Model model, @RequestParam("memberList") String memberList) {
        try {
            JSArrayParser<MemberModel> arrayParser = new JSArrayParser<MemberModel>();
            Collection<MemberModel> parsedMemberList = arrayParser.parse(memberList, MemberModel.class);
            MemberService.updateMemberByAdmin(parsedMemberList);
            return "redirect:/admin/member";
        } catch (Exception e) {
            throw e;
//            return "/error";
        }
    }

    @PostMapping("/admin/member") // URL 주소
    public String memberSearch(Model model, @RequestParam String param, @RequestParam String searchWord) {
        Collection<MemberModel> memberList = MemberService.getMemberBySearch(param, searchWord);
        model.addAttribute("memberList", memberList);
        return "/admin/member"; // JSP 파일명
    }
    /*
        슬라이더 변경
     */
    @GetMapping("/admin/slider") // 비밀번호 변경 테스트
    public String sliderModifyView(Model model) {
        return "/admin/slider-modify"; // JSP 파일명
    }

    @PostMapping("/admin/slider/mod.do") // URL 주소
    public String sliderModify(Model model, @ModelAttribute UploadSliderForm uploadedData) throws IOException {
        JSArrayParser<SliderSettingModel> parser = new JSArrayParser<SliderSettingModel>();
        ArrayList<SliderSettingModel> sliderList= parser.parse(uploadedData.getSliderjson(), SliderSettingModel.class, "slides");
        if (sliderSettingService.modifySlider(sliderList, uploadedData.getFiles())){
            try {
                return "redirect:/admin/slider"; // JSP 파일명
            } catch (Exception ex) {
                throw ex;

            }
        } else {
            return "/error"; // 에러 발생
        }

    }
    /*
        소개 페이지 변경
    */
    @GetMapping("/admin/intro") // 비밀번호 변경 테스트
    public String introModView(Model model) {
        return "/admin/intro-modify"; // JSP 파일명
    }

    @PostMapping("/admin/intro/mod.do") // URL 주소
    public String introModify(Model model, @ModelAttribute UploadIntroForm introForm) throws IOException {
        introSettingService.updateIntro(introForm);
        return "redirect:/admin/intro";
    }
    //  테스트용 페이지
}

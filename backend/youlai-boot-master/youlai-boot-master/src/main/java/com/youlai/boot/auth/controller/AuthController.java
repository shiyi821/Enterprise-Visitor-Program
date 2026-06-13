package com.youlai.boot.auth.controller;

import com.youlai.boot.auth.model.LoginReq;
import com.youlai.boot.common.enums.ActionTypeEnum;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.auth.service.AuthService;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.framework.captcha.model.CaptchaInfo;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import com.youlai.boot.system.model.entity.Role;
import com.youlai.boot.system.model.entity.UserRole;
import com.youlai.boot.system.service.RoleService;
import com.youlai.boot.system.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
// ======= 需要新增的引入（如果IDE报错，可直接Alt+Enter自动引入） =======
import com.youlai.boot.system.service.UserService;
import com.youlai.boot.system.model.entity.SysUser;
import org.springframework.security.crypto.password.PasswordEncoder;
// =============================================================

/**
 * 认证控制层
 *
 * @author Ray.Hao
 * @since 0.0.1
 */
@Tag(name = "01.认证中心")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    // ======= 1. 新增注入系统用户服务和密码加密器 =======
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleService sysUserRoleService;

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<CaptchaInfo> getCaptcha() {
        CaptchaInfo captcha = authService.getCaptcha();
        return Result.success(captcha);
    }
    // ======= 2. 编写免验证码注册接口（利用 UserService 写入数据库） =======
    @Operation(summary = "用户注册（免验证码极简版）")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid LoginReq request) {
        // 1) 查重：判断该用户名/手机号是否已被占用
        Long count = userService.lambdaQuery()
            .eq(SysUser::getUsername, request.getUsername())
            .count();
        if (count > 0) {
            return Result.failed("该手机号已被注册，请直接登录");
        }
// 2) 组装新用户实体数据
        SysUser sysUser = new SysUser();
        sysUser.setUsername(request.getUsername());

        // 核心：使用 Spring Security 的 BCrypt 强哈希加密明文密码
        sysUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // 自动提取手机后4位组合成默认昵称
        String phone = request.getUsername();
        sysUser.setNickname("访客_" + (phone.length() > 4 ? phone.substring(phone.length() - 4) : phone));
        sysUser.setMobile(phone);

        // ================== 【核心改动 1】 ==================
        sysUser.setStatus(1);     // 1 - 账号正常激活状态（彻底解决前端显示“禁用”的问题）
        sysUser.setDeptId(null);  // 显式设为 null，确保新注册的外部访客绝对没有部门
        // ===================================================

        // 3) 调用工具类自带的 save 方法，直接保存到数据库
        boolean success = userService.save(sysUser);

        if (success) {
            // ================== 【核心改动 2：自动绑定外部访客角色】 ==================
            // 因为上面 save 成功后，MyBatis-Plus 会自动将生成的自增主键回填到 sysUser 对象中
            Long newUserId = sysUser.getId();
            Long visitorRoleId = 3L; // 我们在数据库里为“外部访客”设定的角色 ID

            // 创建中间表实体对象
            UserRole userRole = new UserRole();
            userRole.setUserId(newUserId);
            userRole.setRoleId(visitorRoleId);

            // 写入 sys_user_role 表
            sysUserRoleService.save(userRole);
            // ====================================================================

            return Result.success();
        } else {
            return Result.failed("注册失败，请稍后再试");
        }

    }
    // ===================================================================
    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGIN)
    public Result<AuthenticationToken> login(@RequestBody @Valid LoginReq request) {
        AuthenticationToken authenticationToken = authService.login(request.getUsername(), request.getPassword());
        return Result.success(authenticationToken);
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/login/sms")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGIN)
    public Result<AuthenticationToken> loginBySms(
            @Parameter(description = "手机号", example = "18888888888") @RequestParam String mobile,
            @Parameter(description = "验证码", example = "123456") @RequestParam String code
    ) {
        AuthenticationToken loginResult = authService.loginBySms(mobile, code);
        return Result.success(loginResult);
    }

    @Operation(summary = "发送登录短信验证码")
    @PostMapping("/sms/code")
    public Result<Void> sendSmsCode(
            @Parameter(description = "手机号", example = "18888888888") @RequestParam String mobile
    ) {
        authService.sendSmsCode(mobile);
        return Result.success();
    }

    @Operation(summary = "退出登录")
    @DeleteMapping("/logout")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGOUT)
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh-token")
    public Result<AuthenticationToken> refreshToken(
            @Parameter(description = "刷新令牌", example = "xxx.xxx.xxx") @RequestParam String refreshToken
    ) {
        AuthenticationToken authenticationToken = authService.refreshToken(refreshToken);
        return Result.success(authenticationToken);
    }

}

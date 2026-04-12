package com.vod.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vod.server.config.VodSourceConfig;
import com.vod.server.exception.ErrorCodeEnum;
import com.vod.server.exception.ServiceException;
import com.vod.server.security.context.UserContext;
import com.vod.server.service.IVodSourceConfigService;
import com.vod.server.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/source")
@RequiredArgsConstructor
public class VodSourceConfigController {

    private final IVodSourceConfigService sourceConfigService;

    private void assertAdmin() {
        if (!Boolean.TRUE.equals(UserContext.getIsAdmin())) {
            throw new ServiceException(ErrorCodeEnum.FORBIDDEN, "仅管理员可操作该接口");
        }
    }

    @GetMapping("/list")
    public Result<Page<VodSourceConfig>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sourceName) {
        assertAdmin();
        LambdaQueryWrapper<VodSourceConfig> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(sourceName)) {
            queryWrapper.like(VodSourceConfig::getSourceName, sourceName);
        }
        queryWrapper.orderByDesc(VodSourceConfig::getCreateTime);
        Page<VodSourceConfig> pageResult = sourceConfigService.page(new Page<>(page, size), queryWrapper);
        return Result.success(pageResult);
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody VodSourceConfig config) {
        assertAdmin();
        if (!StringUtils.hasText(config.getSourceName()) || !StringUtils.hasText(config.getApiUrl())) {
            throw new ServiceException(ErrorCodeEnum.PARAM_ERROR, "sourceName 与 apiUrl 不能为空");
        }
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getParseType() == null) {
            config.setParseType(1);
        }
        if (config.getSortWeight() == null) {
            config.setSortWeight(0);
        }
        boolean success = sourceConfigService.save(config);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody VodSourceConfig config) {
        assertAdmin();
        if (config.getId() == null) {
            throw new ServiceException(ErrorCodeEnum.PARAM_ERROR, "id不能为空");
        }
        boolean success = sourceConfigService.updateById(config);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    @PutMapping("/status/{id}/{status}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        assertAdmin();
        VodSourceConfig config = new VodSourceConfig();
        config.setId(id);
        config.setStatus(status);
        boolean success = sourceConfigService.updateById(config);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        assertAdmin();
        boolean success = sourceConfigService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}

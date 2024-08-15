package org.placeholder.homerback.dtos;

import java.util.List;

public class ActionsDTO {
    private Long totalCount;
    private Integer page;
    private List<ActionDTO> actions;
    public ActionsDTO(Long totalCount, Integer page, List<ActionDTO> actions) {
        this.totalCount = totalCount;
        this.page = page;
        this.actions = actions;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public List<ActionDTO> getActions() {
        return actions;
    }

    public void setActions(List<ActionDTO> actions) {
        this.actions = actions;
    }
}

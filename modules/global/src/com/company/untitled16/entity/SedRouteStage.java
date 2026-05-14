package com.company.untitled16.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@NamePattern("%s. %s|orderNo,name")
@Table(name = "UNTITLED16_SED_ROUTE_STAGE")
@Entity(name = "untitled16_SedRouteStage")
public class SedRouteStage extends StandardEntity {
    private static final long serialVersionUID = 7466847993435811534L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROUTE_ID", nullable = false)
    protected SedBusinessRoute route;

    @Column(name = "ORDER_NO", nullable = false)
    protected Integer orderNo;

    @Column(name = "NAME", nullable = false, length = 255)
    protected String name;

    @Column(name = "RESPONSIBLE_ROLE", nullable = false, length = 100)
    protected String responsibleRole;

    @Column(name = "SLA_HOURS")
    protected Integer slaHours;

    @Column(name = "DECISION_POLICY", nullable = false, length = 20)
    protected String decisionPolicy;

    @Lob
    @Column(name = "INSTRUCTIONS")
    protected String instructions;

    public SedBusinessRoute getRoute() {
        return route;
    }

    public void setRoute(SedBusinessRoute route) {
        this.route = route;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponsibleRole() {
        return responsibleRole;
    }

    public void setResponsibleRole(String responsibleRole) {
        this.responsibleRole = responsibleRole;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }

    public SedDecisionPolicy getDecisionPolicy() {
        return decisionPolicy == null ? null : SedDecisionPolicy.fromId(decisionPolicy);
    }

    public void setDecisionPolicy(SedDecisionPolicy decisionPolicy) {
        this.decisionPolicy = decisionPolicy == null ? null : decisionPolicy.getId();
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}

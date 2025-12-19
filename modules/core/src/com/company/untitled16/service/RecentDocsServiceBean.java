package com.company.untitled16.service;

import com.company.untitled16.entity.RecentDoc;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service(RecentDocsService.NAME)
public class RecentDocsServiceBean implements RecentDocsService {

    @Override
    public void remove(String entityName, UUID entityId) {
        if (entityName == null || entityName.trim().isEmpty() || entityId == null) return;

        UUID userId = userSessionSource.getUserSession().getUser().getId();

        try (Transaction tx = persistence.createTransaction()) {
            com.haulmont.cuba.core.EntityManager em = persistence.getEntityManager();

            List<RecentDoc> rows = em.createQuery(
                    "select e from untitled16_RecentDoc e " +
                            "where e.user.id = :uid and e.entityName = :en and e.entityId = :eid",
                    RecentDoc.class)
                    .setParameter("uid", userId)
                    .setParameter("en", entityName)
                    .setParameter("eid", entityId)
                    .getResultList();

            for (RecentDoc r : rows) {
                em.remove(r);
            }

            tx.commit();
        }
    }


    private static final int HARD_LIMIT = 10;

    @Inject private Persistence persistence;
    @Inject private Metadata metadata;
    @Inject private UserSessionSource userSessionSource;

    @Override
    public void register(String entityName, UUID entityId, String caption) {
        if (entityName == null || entityName.trim().isEmpty() || entityId == null) return;

        UUID userId = userSessionSource.getUserSession().getUser().getId();

        String cap = caption != null ? caption.trim() : "";
        if (cap.length() > 255) cap = cap.substring(0, 255);

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            // 1) убрать дубль (soft delete через em.remove)
            List<RecentDoc> dups = em.createQuery(
                    "select e from untitled16_RecentDoc e " +
                            "where e.user.id = :uid and e.entityName = :en and e.entityId = :eid",
                    RecentDoc.class)
                    .setParameter("uid", userId)
                    .setParameter("en", entityName)
                    .setParameter("eid", entityId)
                    .getResultList();

            for (RecentDoc d : dups) {
                em.remove(d);
            }

            // 2) добавить свежую запись
            RecentDoc rd = metadata.create(RecentDoc.class);
            rd.setUser(em.getReference(User.class, userId));
            rd.setEntityName(entityName);
            rd.setEntityId(entityId);
            rd.setCaption(cap);
            rd.setVisitedTs(new Date());
            em.persist(rd);

            // 3) обрезать хвост (оставить 10 последних)
            List<RecentDoc> old = em.createQuery(
                    "select e from untitled16_RecentDoc e " +
                            "where e.user.id = :uid order by e.visitedTs desc",
                    RecentDoc.class)
                    .setParameter("uid", userId)
                    .setFirstResult(HARD_LIMIT)
                    .setMaxResults(1000)
                    .getResultList();

            for (RecentDoc o : old) {
                em.remove(o);
            }

            tx.commit();
        }
    }

    @Override
    public List<RecentDocInfo> loadLast(int limit) {
        int lim = Math.max(1, Math.min(limit, HARD_LIMIT));
        UUID userId = userSessionSource.getUserSession().getUser().getId();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = (EntityManager) persistence.getEntityManager();

            List<Object[]> rows = em.createQuery(
                    "select e.entityName, e.entityId, e.caption, e.visitedTs " +
                            "from untitled16_RecentDoc e " +
                            "where e.user.id = :uid " +
                            "order by e.visitedTs desc",
                    Object[].class)
                    .setParameter("uid", userId)
                    .setMaxResults(lim)
                    .getResultList();

            tx.commit();

            List<RecentDocInfo> res = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                res.add(new RecentDocInfo(
                        (String) r[0],
                        (UUID) r[1],
                        (String) r[2],
                        (Date) r[3]
                ));
            }
            return res;
        }
    }
}

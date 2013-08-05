package com.andrada.sitracker.db.dao;

import com.andrada.sitracker.db.beans.Author;
import com.andrada.sitracker.db.beans.Publication;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ggodonoga on 07/06/13.
 */
public class PublicationDaoImpl extends BaseDaoImpl<Publication, Integer>
        implements PublicationDao {


    public PublicationDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, Publication.class);
    }

    @Override
    public List<Publication> getPublicationsForAuthor(Author author) throws SQLException {
        return getPublicationsForAuthorId(author.getId());
    }

    @Override
    public List<Publication> getPublicationsForAuthorId(long authorId) throws SQLException {
        return this.queryBuilder().where().eq("author_id", authorId).query();
    }

    @Override
    public List<Publication> getNewPublicationsForAuthor(Author author) throws SQLException {
        return getNewPublicationsForAuthorId(author.getId());
    }

    @Override
    public List<Publication> getNewPublicationsForAuthorId(long authorId) throws SQLException {
        return this.queryBuilder().where()
                .eq("author_id", authorId)
                .and()
                .eq("isNew", true).query();
    }

    @Override
    public long getNewPublicationsCountForAuthor(Author author) throws SQLException {
        return getNewPublicationsCountForAuthorId(author.getId());
    }

    @Override
    public long getNewPublicationsCountForAuthorId(long authorId) throws SQLException {
        return this.queryBuilder().where()
                .eq("author_id", authorId)
                .and()
                .eq("isNew", true).countOf();
    }

    @Override
    public List<Publication> getSortedPublicationsForAuthorId(long authorId) throws SQLException {
        return this.queryBuilder()
                .orderBy("isNew", false)
                .orderBy("updateDate", false)
                .orderBy("category", true)
                .where().eq("author_id", authorId)
                .query();
    }
}
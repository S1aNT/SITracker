package com.andrada.sitracker.fragment.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.andrada.sitracker.components.PublicationCategoryItemView;
import com.andrada.sitracker.components.PublicationCategoryItemView_;
import com.andrada.sitracker.components.PublicationItemView;
import com.andrada.sitracker.components.PublicationItemView_;
import com.andrada.sitracker.db.beans.Publication;
import com.andrada.sitracker.db.manager.SiDBHelper;
import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.RootContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggodonoga on 05/06/13.
 */

@EBean
public class PublicationsAdapter extends BaseExpandableListAdapter {

    List<String> mCategories = new ArrayList<String>();
    List<List<Publication>> mChildren = new ArrayList<List<Publication>>();

    @OrmLiteDao(helper = SiDBHelper.class, model = Publication.class)
    Dao<Publication, Integer> publicationsDao;

    @RootContext
    Context context;

    public void reloadPublicationsForAuthorId(long id) {
        try {
            createChildList(publicationsDao.queryBuilder().where().eq("authorID", id).query());
            notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createChildList(List<Publication> items) {
        mCategories.clear();
        mChildren.clear();

        for (Publication publication : items) {
            if(!mCategories.contains(publication.getCategory())){
                mCategories.add(publication.getCategory());
            }
        }

        for (String category : mCategories) {
            List<Publication> categoryList = new ArrayList<Publication>();
            for (Publication publication : items) {
                if (publication.getCategory().equals(category)) {
                    categoryList.add(publication);
                }
            }
            mChildren.add(categoryList);
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<Publication> items = mChildren.get(groupPosition);
        return items.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        List<Publication> items = mChildren.get(groupPosition);
        return items.get(childPosition).getId();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        PublicationItemView publicationItemView;
        if (convertView == null) {
            publicationItemView = PublicationItemView_.build(context);
        } else {
            publicationItemView = (PublicationItemView) convertView;
        }
        Boolean isLast = mChildren.get(groupPosition).size() -1 == childPosition;
        publicationItemView.bind((Publication)getChild(groupPosition, childPosition), isLast);

        return publicationItemView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        PublicationCategoryItemView publicationCategoryView;
        if (convertView == null) {
            publicationCategoryView = PublicationCategoryItemView_.build(context);
        } else {
            publicationCategoryView = (PublicationCategoryItemView) convertView;
        }
        publicationCategoryView.bind(mCategories.get(groupPosition));
        return publicationCategoryView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildren.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mCategories.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mCategories.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

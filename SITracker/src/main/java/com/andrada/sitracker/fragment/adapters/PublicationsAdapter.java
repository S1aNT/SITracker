package com.andrada.sitracker.fragment.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.andrada.sitracker.Constants;
import com.andrada.sitracker.contracts.IsNewItemTappedListener;
import com.andrada.sitracker.db.beans.Publication;
import com.andrada.sitracker.db.dao.PublicationDao;
import com.andrada.sitracker.db.manager.SiDBHelper;
import com.andrada.sitracker.events.PublicationMarkedAsReadEvent;
import com.andrada.sitracker.fragment.components.PublicationCategoryItemView;
import com.andrada.sitracker.fragment.components.PublicationCategoryItemView_;
import com.andrada.sitracker.fragment.components.PublicationItemView;
import com.andrada.sitracker.fragment.components.PublicationItemView_;
import com.google.analytics.tracking.android.EasyTracker;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by ggodonoga on 05/06/13.
 */

@EBean
public class PublicationsAdapter extends BaseExpandableListAdapter implements
        IsNewItemTappedListener, AdapterView.OnItemLongClickListener {

    List<String> mCategories = new ArrayList<String>();
    List<List<Publication>> mChildren = new ArrayList<List<Publication>>();

    @OrmLiteDao(helper = SiDBHelper.class, model = Publication.class)
    PublicationDao publicationsDao;

    @RootContext
    Context context;

    ListView listView = null;

    @Background
    public void reloadPublicationsForAuthorId(long id) {
        try {

            List<Publication> pubs = publicationsDao.getSortedPublicationsForAuthorId(id);
            List<String> newCategories = new ArrayList<String>();
            List<List<Publication>> newChildren = new ArrayList<List<Publication>>();

            for (Publication publication : pubs) {
                if (!newCategories.contains(publication.getCategory())) {
                    newCategories.add(publication.getCategory());
                }
            }

            for (String category : newCategories) {
                List<Publication> categoryList = new ArrayList<Publication>();
                for (Publication publication : pubs) {
                    if (publication.getCategory().equals(category)) {
                        categoryList.add(publication);
                    }
                }
                newChildren.add(categoryList);
            }
            updateAdapterDataSet(newCategories, newChildren);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void updateAdapterDataSet(List<String> newCategories, List<List<Publication>> newChildren) {
        mCategories = newCategories;
        mChildren = newChildren;
        notifyDataSetChanged();
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
            publicationItemView.setListener(this);
        } else {
            publicationItemView = (PublicationItemView) convertView;
        }
        Boolean isLast = mChildren.get(groupPosition).size() - 1 == childPosition;
        publicationItemView.bind((Publication) getChild(groupPosition, childPosition), isLast);

        return publicationItemView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (listView == null) {
            listView = (ListView) parent;
        }
        PublicationCategoryItemView publicationCategoryView;
        if (convertView == null) {
            publicationCategoryView = PublicationCategoryItemView_.build(context);
        } else {
            publicationCategoryView = (PublicationCategoryItemView) convertView;
        }
        publicationCategoryView.bind(mCategories.get(groupPosition), mChildren.get(groupPosition).size());
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
        return true;
    }

    @Override
    public void onIsNewItemTapped(View checkBox) {
        if (listView != null) {
            Publication pub = (Publication) checkBox.getTag();
            updateStatusOfPublication(pub);
        }
    }

    private void updateStatusOfPublication(Publication pub) {
        if (pub != null && pub.getNew()) {
            pub.setNew(false);
            pub.setOldSize(0);
            try {
                publicationsDao.update(pub);
                EventBus.getDefault().post(new PublicationMarkedAsReadEvent(pub.getId()));
            } catch (SQLException e) {
                EasyTracker.getTracker().sendException("Publication Set update", e, false);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            long packedPosition = ((ExpandableListView) parent).getExpandableListPosition(position);
            int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
            List<Publication> items = mChildren.get(groupPosition);
            Publication pub = items.get(childPosition);
            updateStatusOfPublication(pub);

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(pub.getUrl()));
            context.startActivity(i);
            EasyTracker.getTracker().sendEvent(
                    Constants.GA_UI_CATEGORY,
                    Constants.GA_EVENT_AUTHOR_PUB_OPEN,
                    Constants.GA_EVENT_AUTHOR_PUB_OPEN, null);
            EasyTracker.getInstance().dispatch();
            // Return true as we are handling the event.
            return true;
        }

        return false;
    }
}
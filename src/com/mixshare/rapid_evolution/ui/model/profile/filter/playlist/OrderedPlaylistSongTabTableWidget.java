package com.mixshare.rapid_evolution.ui.model.profile.filter.playlist;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.search_tabs.OrderedPlaylistTabTableView;

public class OrderedPlaylistSongTabTableWidget extends PlaylistSongTabTableWidget {

	public OrderedPlaylistSongTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
    protected SortTableView createTableView(TableModelManager modelManager) {
    	return new OrderedPlaylistTabTableView((RecordTableModelManager)modelManager);
    }
	
}

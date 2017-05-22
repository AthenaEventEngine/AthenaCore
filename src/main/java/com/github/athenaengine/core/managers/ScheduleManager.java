/*
 * Copyright (C) 2015-2016 L2J EventEngine
 *
 * This file is part of L2J EventEngine.
 *
 * L2J EventEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J EventEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.athenaengine.core.managers;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

import com.github.athenaengine.core.interfaces.tasks.IScheduledTask;
import com.github.athenaengine.core.interfaces.tasks.IRepetitiveTask;
import com.l2jserver.gameserver.ThreadPoolManager;

public class ScheduleManager {

	// Event time
	private int mCurrentTime;

	// Scheduled events
	private final Map<Integer, List<IScheduledTask>> mScheduledTasks = new HashMap<>();

	// Task that control the event time
	private ScheduledFuture<?> mTaskControlTime;

	// Map of repetitive tasks
	private Map<IRepetitiveTask, ScheduledFuture<?>> mRepetitiveTasks = new HashMap<>();
	
	public void startTaskControlTime() {
		mCurrentTime = 0;
		mTaskControlTime = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			mCurrentTime += 1000;
			checkSchedules();
		}, 10 * 1000, 1000);
	}
	
	public ScheduledFuture<?> getTaskControlTime() {
		return mTaskControlTime;
	}
	
	public void addSchedule(IScheduledTask event) {
		if (!mScheduledTasks.containsKey(event.getTime())) {
			mScheduledTasks.put(event.getTime(), new ArrayList<>());
		}

		mScheduledTasks.get(event.getTime()).add(event);
	}

	public void addRepetitiveTask(IRepetitiveTask scheduleRepetitive) {
		ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(
				() -> scheduleRepetitive.run(),
				scheduleRepetitive.getInitialDelay(),
				scheduleRepetitive.getPeriod()
		);

		mRepetitiveTasks.put(scheduleRepetitive, task);
	}

	public void stopRepetitiveTask(IRepetitiveTask scheduleRepetitive) {
		ScheduledFuture task = mRepetitiveTasks.remove(scheduleRepetitive);
		if (task != null) task.cancel(true);
	}

	public void clean() {
		cancelTaskControlTime();
		cleanRepetitiveTasks();
	}

	private void cancelTaskControlTime() {
		mTaskControlTime.cancel(true);
	}

	private void cleanRepetitiveTasks() {
		for (ScheduledFuture<?> task : mRepetitiveTasks.values()) {
			task.cancel(true);
		}

		mRepetitiveTasks.clear();
	}
	
	private void checkSchedules() {
		List<IScheduledTask> list = mScheduledTasks.get(mCurrentTime);
		if (list != null) {
			for (IScheduledTask event : list) event.run();
		}
	}
}
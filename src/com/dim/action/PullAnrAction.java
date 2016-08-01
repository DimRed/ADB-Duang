package com.dim.action;

import com.dim.DeviceResult;
import com.dim.comand.LsCommand;
import com.dim.comand.PullCommand;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.dim.ui.NotificationHelper.error;
import static com.dim.ui.NotificationHelper.info;


/**
 * Created by dim on 16/3/31.
 */
public class PullAnrAction
	extends BaseAction {
	@Override
	void run(final DeviceResult deviceResult, AnActionEvent anActionEvent) {
		//异步获取, 因为adb 获取权限是一个同步操作.如果手机长时间设置允许,idea 将一直阻塞.
		ProgressManager.getInstance()
		               .run(new Task.Backgroundable(deviceResult.anActionEvent.getProject(),
		                                            "PullAnrAction") {
			               @Override
			               public void run(@NotNull ProgressIndicator progressIndicator) {
				               progressIndicator.setIndeterminate(true);

				               final String dataPath = "/data/anr/";
				               LsCommand ls =
					               new LsCommand(deviceResult,
					                             dataPath,
					                             "*" + deviceResult.packageName + "*.txt");
				               ls.run();
				               final List<String> list = ls.getResult();
				               if (list.size() > 0) {
					               info("starting to pull " + list.size() + " anr files");
					               boolean ret = true;
					               PullCommand pullCommand = null;
					               for (String item : list) {
						               //数据库路径
						               pullCommand = new PullCommand(deviceResult, dataPath, item, "anr");
						               ret &= pullCommand.run();
					               }
					               if (ret) {
						               pullCommand.scrollToTargetSource();
						               info("pull anr success!");
					               } else {
						               error("pull anr failed.");
					               }
				               } else {
					               info(deviceResult.packageName + "has no anr file.");
				               }
			               }
		               });
	}
}

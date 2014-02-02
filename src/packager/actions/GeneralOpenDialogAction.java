package packager.actions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;

import packager.entity.FileElement;
import packager.ui.GitFilesDialog;

public abstract class GeneralOpenDialogAction extends ActionDelegate implements IViewActionDelegate {

	private IStructuredSelection selection = null;

	private IViewPart viewPart;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart viewPart) {
		this.viewPart = viewPart;
	}

	@Override
	public void run(IAction action) {
		if (this.selection.getFirstElement() instanceof IProject) {
			action.setEnabled(true);
			IProject project = (IProject) this.selection.getFirstElement();
			GitFilesDialog dialog = new GitFilesDialog(this.viewPart.getSite().getShell(), project,this.run(project));
			dialog.open();
		} else {
			action.setEnabled(false);
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		}
	}

	private List<FileElement> run(IProject project) {
		List<FileElement> result = new ArrayList<FileElement>();
		Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
		try {
			
			Process p = run.exec(this.getCommand(), null, project.getLocation().toFile());// 启动另一个进程来执行命令
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null){
				// 获得命令执行后在控制台的输出信息
				String line = lineStr.trim();
				if(line.indexOf(" ") != -1){
					FileElement element = new FileElement();
					element.setState(line.substring(0,line.indexOf(" ")).trim());
					element.setSrc(line.substring(line.lastIndexOf(" ")).trim());
					result.add(element);
				}
			}
			// 检查命令是否执行失败。
			if (p.waitFor() != 0 && p.exitValue() == 1){// p.exitValue()==0表示正常结束，1：非正常结束
					throw new RuntimeException("命令执行失败!");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {

		}
		return result;
	}
	
	protected abstract String getCommand();

}

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
		Runtime run = Runtime.getRuntime();// �����뵱ǰ Java Ӧ�ó�����ص�����ʱ����
		try {
			
			Process p = run.exec(this.getCommand(), null, project.getLocation().toFile());// ������һ��������ִ������
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null){
				// �������ִ�к��ڿ���̨�������Ϣ
				String line = lineStr.trim();
				if(line.indexOf(" ") != -1){
					FileElement element = new FileElement();
					element.setState(line.substring(0,line.indexOf(" ")).trim());
					element.setSrc(line.substring(line.lastIndexOf(" ")).trim());
					result.add(element);
				}
			}
			// ��������Ƿ�ִ��ʧ�ܡ�
			if (p.waitFor() != 0 && p.exitValue() == 1){// p.exitValue()==0��ʾ����������1������������
					throw new RuntimeException("����ִ��ʧ��!");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {

		}
		return result;
	}
	
	protected abstract String getCommand();

}

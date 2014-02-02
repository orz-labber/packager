/**
 * 
 */
package packager.job;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author obladi
 *
 */
public class ExportFilesJob extends Job {
	
	private IProject project;
	
	private Object[] elements;

	public ExportFilesJob(String name,IProject project,Object[] elements) {
		super(name);
		this.project = project;
		this.elements = elements;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("导出......", this.elements.length);
		for(Object element : this.elements){
				monitor.setTaskName("导出文件"+project.getFullPath().getFileExtension() + element.toString() + "......");
				if(monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}
				System.out.println(element.toString());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				monitor.worked(1);
		}
		monitor.done();
	
		return Status.OK_STATUS;
	}

}

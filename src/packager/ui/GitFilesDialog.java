package packager.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;

import packager.entity.FileElement;

public class GitFilesDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	private IProject project;

	private boolean zipable = false;

	private final List<FileElement> listData;
	private Table table;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public GitFilesDialog(Shell parent, IProject project, List<FileElement> datas) {
		super(parent);
		this.project = project;
		this.listData = datas;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	@SuppressWarnings("rawtypes")
	private void createContents() {
		shell = new Shell(getParent(), SWT.CLOSE);
		shell.setSize(645, 416);
		shell.setText(getText());
		final CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(shell, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION | SWT.MULTI);
		checkboxTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

			}

			public Object[] getElements(Object element) {
				if (element instanceof List) {
					return ((List) element).toArray();
				}
				return new Object[0];
			}

		});
		table = checkboxTableViewer.getTable();
		table.setBounds(0, 35, 645, 359);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableViewerColumn stateColumn = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		TableColumn sc = stateColumn.getColumn();
		sc.setAlignment(SWT.LEFT);
		sc.setWidth(40);
		stateColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				FileElement fileElement = (FileElement) element;
				return fileElement.getState();
			}

		});
		
		
		TableViewerColumn filePathColumn = new TableViewerColumn(checkboxTableViewer, SWT.NONE);
		TableColumn tableColumn = filePathColumn.getColumn();
		tableColumn.setAlignment(SWT.LEFT);
		tableColumn.setWidth(380);
		tableColumn.setText("File List");

		filePathColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				FileElement fileElement = (FileElement) element;
				return fileElement.getSrc();
			}

		});

		final Button button = new Button(shell, SWT.CHECK);
		button.setBounds(80, 5, 52, 18);
		button.setText("zip");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zipable = button.getSelection();
			}

		});

		Button btnDaoChu = new Button(shell, SWT.NONE);
		btnDaoChu.setBounds(127, 1, 94, 28);
		btnDaoChu.setText("export");
		btnDaoChu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
                    shell.setVisible(false);
					new ProgressMonitorDialog(shell).run(false, false, new IRunnableWithProgress() {

						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							Object[] elements = checkboxTableViewer.getCheckedElements();
							if(zipable){
								try {
									zip(elements,monitor);
								} catch (IOException e) {
									e.printStackTrace();
									throw new RuntimeException(e);
								} catch (ArchiveException e) {
									e.printStackTrace();
									throw new RuntimeException(e);
								}
							}else {
								try {
									saveas(elements,monitor);
								} catch (IOException e) {
									e.printStackTrace();
									throw new RuntimeException(e);
								}
							}
						}

					});
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

		});

		checkboxTableViewer.setInput(this.listData);
		final Button selectedAll = new Button(shell, SWT.CHECK);
		selectedAll.setBounds(0, 5, 74, 18);
		selectedAll.setText("select all");
		selectedAll.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkboxTableViewer.setAllChecked(selectedAll.getSelection());
			}

		});

		checkboxTableViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {// бЁжа
					if (checkboxTableViewer.getCheckedElements().length == listData.size()) {
						selectedAll.setSelection(true);
					}
				} else {
					if (checkboxTableViewer.getCheckedElements().length < listData.size()) {
						selectedAll.setSelection(false);
					}
				}
			}

		});

	}
	
	private void zip(Object[] elements,IProgressMonitor monitor) throws IOException, ArchiveException{
		monitor.beginTask("", elements.length);
		String projectPath = this.project.getLocation().toOSString();
		String zipFileName = System.getProperty("user.name") + "_"+ this.parseDate(new Date()) + ".zip";
		File zipFile = new File(projectPath + File.separator + "output" + File.separator + zipFileName);
		zipFile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(zipFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ZipArchiveOutputStream zos = (ZipArchiveOutputStream)new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, bos);
		zos.setEncoding("GB2312");
		for(Object element : elements){
			FileElement e = (FileElement)element;
			monitor.setTaskName(e.getSrc() + " ......");
			File file = new File(projectPath + File.separator + e.getSrc());
			ZipArchiveEntry entry = new ZipArchiveEntry(file.getName());
			zos.putArchiveEntry(entry);
			FileInputStream fis = new FileInputStream(file);
			org.apache.commons.compress.utils.IOUtils.copy(fis, zos);
			zos.closeArchiveEntry();
			fis.close();
			monitor.worked(1);
		}
		zos.close();
		bos.flush();
		fos.close();
		monitor.done();
	}
	
	private void saveas(Object[] elements,IProgressMonitor monitor) throws IOException{
		monitor.beginTask("", elements.length);
		String projectPath = this.project.getLocation().toOSString();
		String savePath = "output/" + System.getProperty("user.name") + "_"+ this.parseDate(new Date());
		for(Object element : elements){
			FileElement e = (FileElement)element;
			monitor.setTaskName(e.getSrc() + " ......");
			FileInputStream fis = new FileInputStream(new File(projectPath + File.separator + e.getSrc()));
			File out = new File(projectPath + File.separator + savePath + File.separator + e.getSrc());
			out.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(out);
			org.apache.commons.io.IOUtils.copy(fis,fos);
			fos.flush();
			fis.close();
			fos.close();

			monitor.worked(1);
		}
		monitor.done();
	}
	
	private String parseDate(Date date) {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		return df.format(date);
	}
	
	
}

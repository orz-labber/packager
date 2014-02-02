package packager.actions;

public class SvnOpenDialogAction extends GeneralOpenDialogAction {

	@Override
	protected String getCommand() {
		return "svn status";
	}

}

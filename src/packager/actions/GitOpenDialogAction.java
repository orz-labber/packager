/**
 * 
 */
package packager.actions;
/**
 * @author obladi
 * 
 */
public class GitOpenDialogAction extends GeneralOpenDialogAction {

	@Override
	protected String getCommand() {
		return "git status -s";
	}

}

package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrepInputFilter extends AbstractGrepFilter implements InputFilter {

	private WeakReference<ConsoleView> console;
	protected volatile boolean lastLineFiltered = false;

	public GrepInputFilter(Project project, Profile profile) {
		super(project, profile);
	}

	public GrepInputFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	public void init(WeakReference<ConsoleView> console, Profile profile) {
		this.profile = profile;
		this.console = console;
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
																  ConsoleViewContentType consoleViewContentType) {
		FilterState state = super.filter(s, -1);
		clearConsole(state);
		return prepareResult(state);
	}

	public void clearConsole(FilterState state) {
		if (state != null && state.isClearConsole()) {
			ConsoleView consoleView = console.get();
			if (consoleView != null) {
				consoleView.clear();
			}
		}
	}

	@Override
	protected boolean continueFiltering(FilterState state) {
		return !state.isMatchesSomething();
	}

	private List<Pair<String, ConsoleViewContentType>> prepareResult(FilterState state) {
		Pair<String, ConsoleViewContentType> result = null;
		if (state != null) {
			if (state.isExclude()) {
				result = new Pair<>(null, null);
				lastLineFiltered = true;
			} else if (profile.isMultilineInputFilter() && !state.isMatchesSomething() && lastLineFiltered) {
				result = new Pair<>(null, null);
			}
		}
		if (result == null) {
			lastLineFiltered = false;
			return null;// input is not changed
		} else {
			return Arrays.asList(result);
		}
	}

	@Override
	public void onChange() {
		super.onChange();
		lastLineFiltered = false;
	}
	/**
	 * just want to see lines that are highlighted. To do this, I add a ".*" item as the last item and set to "Whole line" and "Filter out".
	 * -> must add all items to grepProcessors
	 * TODO separate clearConsole functionality?
	 */
	@Override
	protected void initProcessors() {
		grepProcessors = new ArrayList<>();
		if (profile.isEnabledInputFiltering()) {
			boolean inputFilterExists = false;
			for (GrepExpressionItem grepExpressionItem : profile.getAllGrepExpressionItems()) {
				grepProcessors.add(createProcessor(grepExpressionItem));
				if (grepExpressionItem.isInputFilter() || grepExpressionItem.isClearConsole()) {
					inputFilterExists = true;
				}
			}
			if (!inputFilterExists) {
				grepProcessors.clear();
			}
		}
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem item) {
//		return profile.isEnabledInputFiltering() && (item.isInputFilter() || item.isClearConsole());
		throw new UnsupportedOperationException();
	}

}

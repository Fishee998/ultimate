package local.stalin.plugins.output.nodecounter;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	public static final String PLUGIN_ID = "local.stalin.plugins.output.nodecounter";
	
	// The shared instance
	private static Activator plugin;
	
	public Activator() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}
	
	public static Activator getDefault() {
		return plugin;
	}
}

package io.voluble.michellelib.menu;

/**
 * Safety/performance knobs for the menu engine.
 * <p>
 * Defaults are conservative and focused on correctness/dupe-resistance.
 */
public record MenuSettings(
		/**
		 * Minimum time between accepted click actions for a session, in nanoseconds.
		 */
		long clickDebounceNs,

		/**
		 * If we manually modify cursor/hotbar/bottom inventory during a click, schedule {@code Player#updateInventory()}
		 * next tick to avoid ghost items/desync.
		 */
		boolean syncClientAfterManualMutation
) {
	public static MenuSettings defaults() {
		return new MenuSettings(150_000_000L, true);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private long clickDebounceNs = defaults().clickDebounceNs();
		private boolean syncClientAfterManualMutation = defaults().syncClientAfterManualMutation();

		private Builder() {
		}

		public Builder clickDebounceNs(final long clickDebounceNs) {
			this.clickDebounceNs = clickDebounceNs;
			return this;
		}

		public Builder clickDebounceMillis(final long millis) {
			return clickDebounceNs(millis * 1_000_000L);
		}

		public Builder syncClientAfterManualMutation(final boolean syncClientAfterManualMutation) {
			this.syncClientAfterManualMutation = syncClientAfterManualMutation;
			return this;
		}

		public MenuSettings build() {
			long ns = clickDebounceNs;
			if (ns < 0L) ns = 0L;
			// Cap at 5 seconds to prevent accidental "my menu is broken" configs
			if (ns > 5_000_000_000L) ns = 5_000_000_000L;
			return new MenuSettings(ns, syncClientAfterManualMutation);
		}
	}
}
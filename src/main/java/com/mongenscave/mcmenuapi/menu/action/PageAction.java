package com.mongenscave.mcmenuapi.menu.action;

import com.mongenscave.mcmenuapi.McMenuAPI;
import com.mongenscave.mcmenuapi.menu.Menu;
import com.mongenscave.mcmenuapi.menu.PaginatedMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Action to change page in a paginated menu
 *
 * Examples:
 * - "[PAGE] +1"  - Next page
 * - "[PAGE] -1"  - Previous page
 * - "[PAGE] 0"   - Go to first page
 * - "[PAGE] 5"   - Go to page 5
 */
@RequiredArgsConstructor
public class PageAction implements Action {

    private final String pageChange;

    @Override
    public void execute(@NotNull Player player) {
        Menu menu = McMenuAPI.getInstance().getOpenMenu(player);

        if (!(menu instanceof PaginatedMenu paginatedMenu)) {
            return; // Not a paginated menu
        }

        int currentPage = paginatedMenu.getCurrentPage(player);
        int newPage = calculateNewPage(currentPage, paginatedMenu.getTotalPages());

        if (newPage >= 0 && newPage < paginatedMenu.getTotalPages()) {
            paginatedMenu.setPage(player, newPage);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }

    private int calculateNewPage(int currentPage, int totalPages) {
        String change = pageChange.trim();

        // Relative changes
        if (change.startsWith("+")) {
            int delta = Integer.parseInt(change.substring(1));
            return currentPage + delta;
        } else if (change.startsWith("-")) {
            int delta = Integer.parseInt(change.substring(1));
            return currentPage - delta;
        } else {
            // Absolute page
            return Integer.parseInt(change);
        }
    }
}
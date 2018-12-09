/*
 * Copyright (c) 2018, TheStonedTurtle <www.github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.loottracker.ui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.loottracker.data.LootTrackerItemEntry;
import net.runelite.client.plugins.loottracker.data.UniqueItem;
import net.runelite.client.plugins.loottracker.data.UniqueItemWithLinkedId;
import net.runelite.client.ui.ColorScheme;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Map;

@Getter
class UniqueItemPanel extends JPanel
{
	private ItemManager itemManager;
	private Collection<UniqueItemWithLinkedId> items;
	private Map<Integer, LootTrackerItemEntry> loots;

	private final float alphaMissing = 0.35f;
	private final float alphaHas = 1.0f;

	private static final Border panelBorder = new EmptyBorder(3, 0, 3, 0);
	private static final Color panelBackgroundColor = ColorScheme.DARK_GRAY_COLOR;

	UniqueItemPanel(Collection<UniqueItemWithLinkedId> items, Map<Integer, LootTrackerItemEntry> loots, ItemManager itemManager)
	{
		this.items = items;
		this.loots = loots;
		this.itemManager = itemManager;


		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(new EmptyBorder(3, 0, 3, 0));

		this.setLayout(new BorderLayout());
		this.setBorder(panelBorder);
		this.setBackground(panelBackgroundColor);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 20;

		// Add each Unique Item icon to the panel
		for (UniqueItemWithLinkedId l : items)
		{
			UniqueItem item = l.getUniqueItem();
			int id = item.getItemID();
			int linkedID = l.getLinkedID();
			LootTrackerItemEntry it = loots.get(id);
			LootTrackerItemEntry notedIt = loots.get(linkedID);
			int quantity = (it == null ? 0 : it.getQuantity()) + (notedIt == null ? 0 : notedIt.getQuantity());
			boolean shouldStack = quantity > 1 || (it != null && it.isStackable());
			final float alpha = (quantity > 0 ? alphaHas : alphaMissing);
			AsyncBufferedImage image = itemManager.getImage(id, quantity, shouldStack);
			BufferedImage opaque = createOpaqueImage(image, alpha);

			// Attach Image to Label and append label to Panel
			ImageIcon o = new ImageIcon(opaque);
			JLabel icon = new JLabel(o);
			panel.add(icon, c);
			c.gridx++;

			// in case the image is blank we will refresh it upon load
			// Should only trigger if image hasn't been added
			Runnable task = () ->
			{
				refreshImage(icon, image, alpha);
			};
			image.onChanged(() -> SwingUtilities.invokeLater(task));

			icon.setToolTipText(item.getName());
		}

		this.add(panel, BorderLayout.NORTH);
	}

	// Used to refresh the item icon if the image was still loading when attempting to create it earlier
	private void refreshImage(JLabel label, AsyncBufferedImage image, float finalAlpha)
	{
		BufferedImage opaque = createOpaqueImage(image, finalAlpha);
		ImageIcon o = new ImageIcon(opaque);

		label.setIcon(o);
		label.revalidate();
		label.repaint();
	}

	// Creates the Item Icon with opacity depending on if they have received the item or not
	private BufferedImage createOpaqueImage(AsyncBufferedImage image, float alpha)
	{
		BufferedImage x = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)x.getGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.drawImage(image, null, 0, 0);
		g2d.dispose();
		return x;
	}
}
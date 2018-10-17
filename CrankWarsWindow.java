import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

@SuppressWarnings("serial") // No plans to add a serialVersionUID
public class CrankWarsWindow extends Frame {
	private int maxSpace = 20;
	private int space = maxSpace;
	private int cash = 2000;
	private int debt = 2000;
	private int bank = 0;
	private boolean haveGun = false;
	private int ammo = 0;
	private int health = 100;
	private int day = 1;
	private String[] locations = {
		"Manhattan", 
		"Brooklyn", 
		"Queens", 
		"The Bronx",
		"Staten Island"
	};
	private int curLocation = 0;
	private String[] drugs = { // Do try to keep these alphabetical
		"Acid", 
		"Cocaine", 
		"Crank",
		"DMT",
		"Heroin",
		"Meth",
		"Opium", 
		"Oxy", 
		"PCP", 
		"Shrooms",
		"Weed"
	};
	private HashMap<String, Integer> drugBasePrices = new HashMap<>(drugs.length);
	private HashMap<String, Integer> drugLocalPrices = new HashMap<>(drugs.length);
	private HashMap<String, Integer> drugsInPocket = new HashMap<>(drugs.length);
	private HashMap<String, Integer> pocketDrugPrices = new HashMap<>(drugs.length);
	Label locationLabel, dayLabel, spaceLabel, cashLabel, debtLabel, bankLabel, ammoLegendLabel,
		ammoLabel, healthLabel;
	TextArea tickerBox;
	List sellPrices, buyPrices;
	Dialog jetDialog;
	MenuBar mainWindowMenu;
	Menu shopMenu;
	MenuItem shopBuyGun, shopBuyAmmo, shopBuy10Ammo, shopBuy10Space;
	
	// Primary Game Window
	public CrankWarsWindow () {
		setLayout(new BorderLayout(3, 3));
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				System.exit(0);  // Terminate the program
			}
		});
		
		// Make Menus
		// TODO: "cheat menu" so I can more easily test things like paying back loans
		// TODO: Hospital for healing
		mainWindowMenu = new MenuBar();
		shopMenu = new Menu("Shop");
		shopBuyGun = new MenuItem("Buy Gun ($100)");
		shopBuyAmmo = new MenuItem("Buy Ammo ($10)");
		shopBuy10Ammo = new MenuItem("Buy 10 Ammo ($100)");
		shopBuy10Space = new MenuItem("Buy 20 Space ($40)");
		// Make Menu listeners
		shopBuyGun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (cash < 100) {
					tickerBox.append("You can't afford a piece.\n");
					return;
				}
				cash -= 100;
				haveGun = true;
				shopMenu.remove(shopBuyGun);
				tickerBox.append("Do you feel lucky, punk?\n");
				refreshStats();
				shopMenu.add(shopBuyAmmo);
				shopMenu.add(shopBuy10Ammo);
				/*
				 * IIRC, the original game did not charge you pocket space for guns or bullets.
				 * That's why they don't now. It might be interesting to see what the game would
				 * look like if they DID take up space. It would definitely make the game harder,
				 * but players might enjoy the challenge and/or realism. I don't know.
				 */
			}
		});
		shopBuyAmmo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (cash < 10) {
					tickerBox.append("You're too broke for bullets.\n");
					return;
				}
				cash -= 10;
				ammo++;
				tickerBox.append("One bullet, coming right up.\n");
				refreshStats();
			}	
		});
		shopBuy10Ammo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (cash < 100) {
					tickerBox.append("You're too broke for bullets.\n");
					return;
				}
				cash -= 100;
				ammo += 10;
				tickerBox.append("You purchase 10 bullets.\n");
				refreshStats();
			}	
		});
		shopBuy10Space.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				int spaceCost = maxSpace * 2;
				if (cash < spaceCost) {
					tickerBox.append("You don't have that kind of money.\n");
					return;
				}
				cash -= spaceCost;
				maxSpace += 20;
				space += 20;
				tickerBox.append("You spend " + spaceCost + " bucks adding pockets, pouches, etc.\n");
				shopBuy10Space.setLabel("Buy 20 Space ($" + (maxSpace * 2) + ")");
				refreshStats();
			}	
		});
		// Populate Menus
		mainWindowMenu.add(shopMenu);
		shopMenu.add(shopBuyGun);
		shopMenu.add(shopBuy10Space);
		setMenuBar(mainWindowMenu);
		
		
		// Set base prices for the various drugs
		// There's probably a better way to do this
		drugBasePrices.put("Acid", 15);
		drugBasePrices.put("Cocaine", 150);
		drugBasePrices.put("Weed", 10);
		drugBasePrices.put("Heroin", 200);
		drugBasePrices.put("Meth", 70);
		drugBasePrices.put("DMT", 300);
		drugBasePrices.put("Shrooms", 20);
		drugBasePrices.put("Crank", 18);
		drugBasePrices.put("Opium", 100); 
		drugBasePrices.put("PCP", 125);
		drugBasePrices.put("Oxy", 150);
		
		// Stats
		// To update, use refreshStats()
		Panel statsPanel = new Panel(new GridLayout(0, 6));
		statsPanel.add(new Label("Location:"));
		locationLabel = new Label(locations[curLocation]);
		statsPanel.add(locationLabel);
		statsPanel.add(new Label("Day:"));
		dayLabel = new Label(day + "");
		statsPanel.add(dayLabel);
		statsPanel.add(new Label("Space:"));
		spaceLabel = new Label(space + " of " + maxSpace);
		statsPanel.add(spaceLabel);
		statsPanel.add(new Label("Cash:"));
		cashLabel = new Label("$" + cash);
		statsPanel.add(cashLabel);
		statsPanel.add(new Label("Debt:"));
		debtLabel = new Label("$" + debt);
		statsPanel.add(debtLabel);
		statsPanel.add(new Label("Bank:"));
		bankLabel = new Label("$" + bank);
		statsPanel.add(bankLabel);
		ammoLegendLabel = new Label("Ammo:");
		statsPanel.add(ammoLegendLabel);
		ammoLabel = new Label(ammo + "");
		statsPanel.add(ammoLabel);
		ammoLabel.setVisible(false);
		ammoLegendLabel.setVisible(false);
		statsPanel.add(new Label("Health:"));
		healthLabel = new Label(health + "%");
		statsPanel.add(healthLabel);
		add(statsPanel, BorderLayout.NORTH);
		
		// News Ticker
		tickerBox = new TextArea(5, 10);
		tickerBox.setEditable(false);
		tickerBox.append("That loan shark gave you $2000, but he's charging 10% interest per day.\n");
		tickerBox.append("If you don't pay him back in 30 days, it's over.\n");
		add(tickerBox, BorderLayout.CENTER);
		
		// Trading Interface
		Panel tradingPanel = new Panel(new BorderLayout(3, 3));
		buyPrices = new List(drugs.length);
		generateBuyPrices();
		tradingPanel.add(buyPrices, BorderLayout.WEST);
		
		Panel tradingButtonsPanel = new Panel(new GridLayout(4, 1));
		// Buy
		Button buyButton = new Button("Buy ->");
		buyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (space == 0) {
					tickerBox.append("And put it where? Sell something, first!\n");
					return;
				}
				String desiredPurchase = buyPrices.getSelectedItem();
				try { // You know, in case the user doesn't select something first
					String[] split = desiredPurchase.split("\\: \\$"); // drug: $price
					String drugName = split[0];
					int price = Integer.parseInt(split[1]);
					if (price > (cash + bank)) {
						tickerBox.append("You can't afford any " + drugName + "!\n");
					}
					else {
						if (price > cash) {
							tickerBox.append("You swing by an ATM, first.\n");
							int withdrawal = price - cash;
							bank -= withdrawal;
							cash += withdrawal;
						}
						cash -= price;
						space--;
						refreshStats();
						
						// How much do we have, if any?
						int amountHeld;
						try {
							amountHeld = drugsInPocket.get(drugName);
						}
						catch (Exception e) {
							amountHeld = 0;
						}
						// How much did that cost, if anything?
						int averagePrice;
						try {
							averagePrice = pocketDrugPrices.get(drugName);
						}
						catch (Exception e) {
							averagePrice = 0;
						}
						averagePrice = (int)Math.rint(((averagePrice * amountHeld) + price + 0.00) / (amountHeld + 1.0));
						drugsInPocket.put(drugName, amountHeld + 1);
						pocketDrugPrices.put(drugName, averagePrice);
						
						refreshSellPrices();
						tickerBox.append("Buying " + drugName + " for $" + price + "\n");
						drugLocalPrices.put(drugName, price + 1);
						refreshBuyPrices();
					}
				}
				catch (Exception e) {
					StackTraceElement l = e.getStackTrace()[0];
					System.out.print("Exception raised while buying drugs: ");
					System.out.println(l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber());
					tickerBox.append("Buy what? Stop smoking your own supply!\n");
				}
			}
		});
		tradingButtonsPanel.add(buyButton);
		// Sell
		Button sellButton = new Button("<- Sell");
		sellButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String desiredPurchase = sellPrices.getSelectedItem();
				try { // You know, in case the user doesn't select something first
					String[] split = desiredPurchase.split("\\: "); // drug: amt @ $price
					String drugName = split[0]; // Throw the rest away. This is the key.
					int price = drugLocalPrices.get(drugName);
					int amountHeld = drugsInPocket.get(drugName) - 1;
					if (amountHeld == 0) {
						drugsInPocket.remove(drugName);
						pocketDrugPrices.remove(drugName);
					}
					else {
						drugsInPocket.put(drugName, amountHeld);
					}
					space++;
					cash += price;
					refreshStats();
					
					refreshBuyPrices();
					tickerBox.append("Selling " + drugName + " for $" + price + "\n");
					drugLocalPrices.put(drugName, price - 2);
					refreshSellPrices();
				}
				catch (Exception e) {
					System.out.print("Exception raised while selling drugs: ");
					e.printStackTrace(System.out);
					tickerBox.append("Sell what? Stop smoking your own supply!\n");
				}
			}
		});
		tradingButtonsPanel.add(sellButton);
		// Jet
		Button jetButton = new Button("Jet");
		jetDialog = new Dialog(this, "Jet to where?", true);
		jetDialog.setLayout(new FlowLayout());
		jetDialog.setSize(250, 85);
		jetDialog.setLocationRelativeTo(null);
		Choice jetChoices = new Choice();
		for (int i = 0; i < locations.length; i++) {
			jetChoices.add(locations[i]);
		}
		Button jetChoicesOkButton = new Button("Jet!");
		jetChoicesOkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jetDialog.setVisible(false);
				for (int i = 0; i < locations.length; i++) {
					if (jetChoices.getSelectedItem().equals(locations[i])) {
						String backOrForth = i == curLocation ? "back" : "out";
						curLocation = i;
						String[] modeOfTravel = {
						                         " hop on the subway ",
						                         " catch a bus ",
						                         " take a cab ",
						                         " bum a ride ",
						                         " head "
												};
						String howYouTraveled = modeOfTravel[((int)(Math.random() * modeOfTravel.length))];
						tickerBox.append("You" + howYouTraveled + backOrForth + " to " + locations[i] + "\n");
						break;
					}
				}
				endOfDay();
			}
		});
		jetDialog.add(jetChoices);
		jetDialog.add(jetChoicesOkButton);
		jetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jetDialog.setVisible(true);
			}
		});
		tradingButtonsPanel.add(jetButton);
		tradingPanel.add(tradingButtonsPanel, BorderLayout.CENTER);
		
		sellPrices = new List();
		refreshSellPrices();
		tradingPanel.add(sellPrices, BorderLayout.EAST);
		add(tradingPanel, BorderLayout.SOUTH);
		
		// Final touches
		setTitle("Crank Wars");
		setSize(500, 700);
		setLocationRelativeTo(null);
		setVisible(true);
		
		
		
		// TODO: drug price events, Officer Hardass, endgame/loan shark, game balance
	}
	
	// Main just fires off the Window and lets it do its thing
	public static void main(String[] args) {
		new CrankWarsWindow();
	}
	
	/**
	 * This method is called to update the stats pane at the top of the primary game window
	 */
	private void refreshStats() {
		locationLabel.setText(locations[curLocation]);
		dayLabel.setText(day + "");
		spaceLabel.setText(space + " of " + maxSpace);
		cashLabel.setText("$" + cash);
		debtLabel.setText("$" + debt);
		bankLabel.setText("$" + bank);
		ammoLabel.setText(ammo + "");
		// Why show ammo stocks for a gun you don't have?
		ammoLabel.setVisible(haveGun);
		ammoLegendLabel.setVisible(haveGun);
		healthLabel.setText(health + "%");
	}
	
	/**
	 * This method is called to generate new prices on arrival at a new location
	 */
	private void generateBuyPrices() {
		buyPrices.removeAll();
		for (int i = 0; i < drugs.length; i++) {
			double priceFactor = Math.random() * 1.8 + 0.2;
			int finalPrice = (int)(drugBasePrices.get(drugs[i]) * priceFactor);
			drugLocalPrices.put(drugs[i], finalPrice);
			buyPrices.add(drugs[i] + ": $" + finalPrice);
		}
	}
	
	/**
	 * This method updates the prices pane without assigning new prices entirely
	 * <p>
	 * Its intended use case is when prices change due to user purchases/sales
	 */
	private void refreshBuyPrices() {
		int selectedIndex = buyPrices.getSelectedIndex();
		buyPrices.removeAll();
		for (int i = 0; i < drugs.length; i++) {
			buyPrices.add(drugs[i] + ": $" + drugLocalPrices.get(drugs[i]));
		}
		buyPrices.select(selectedIndex);
	}
	
	/**
	 * This method is called to update the price and amount of drugs shown in the righthand pane
	 */
	private void refreshSellPrices() {
		int selectedIndex = sellPrices.getSelectedIndex();
		sellPrices.removeAll();
		drugsInPocket.forEach((drug, amount) -> {
			int sellPrice;
			try {
				sellPrice = pocketDrugPrices.get(drug);
			}
			catch (Exception e) {
				// If you're here, it means you somehow have drugs and can't remember what you paid for them.
				System.out.print("Drugs found without associated price: ");
				e.printStackTrace(System.out);
				sellPrice = 0;
				pocketDrugPrices.put(drug, 0); // And don't let it happen again, mister!
				tickerBox.append("You have " + drug + " but don't remember how much that cost.\n");
			}
			sellPrices.add(drug + ": " + amount + " @ " + sellPrice);
		});
		
		if (sellPrices.getItemCount() == 0) {
			sellPrices.add("Your pockets are empty.");
		}
		else {
			sellPrices.select(selectedIndex);
		}
	}
	
	/**
	 * This method advances the current day and handles end-of-day updates/advances
	 */
	private void endOfDay() {
		day++;
		debt = (int)(debt * 1.1); // Will round down for floating points. He's a loan shark, but he's not Satan.
		refreshStats();
		generateBuyPrices();
	}

}

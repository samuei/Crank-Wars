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
	private int playerGunBaseDamage = 10;
	private int ammo = 0;
	private int attackerHP = 0;
	private int health = 100;
	private int day = 1;
	private int dailyHeals = 3;
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
	Menu shopMenu, loanMenu, hospitalMenu, bankMenu;
	MenuItem shopBuyGun, shopBuyAmmo, shopBuy10Ammo, shopBuy10Space, loanPayBack, loanTake, 
		hospitalPartialHeal, hospitalFullHeal, bankDeposit, bankWithdraw;
	
	// Primary Game Window
	public CrankWarsWindow () {
		// Pill icon:
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/pill.png")));
		
		setLayout(new BorderLayout(3, 3));
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				System.exit(0);  // Terminate the program
			}
		});
		
		// Make Menus
		mainWindowMenu = new MenuBar();
		shopMenu = new Menu("Shop");
		shopBuyGun = new MenuItem("Buy Gun ($100)");
		shopBuyAmmo = new MenuItem("Buy Ammo ($10)");
		shopBuy10Ammo = new MenuItem("Buy 10 Ammo ($100)");
		shopBuy10Space = new MenuItem("Buy 20 Space ($40)");
		loanMenu = new Menu("Loan");
		loanPayBack = new MenuItem("Pay Back Loan");
		loanTake = new MenuItem("Take Out New Loan");
		hospitalMenu = new Menu("Hospital");
		hospitalPartialHeal = new MenuItem("Get Minor Treatment ($100)");
		hospitalFullHeal = new MenuItem("Check Yourself In ($1500)");
		bankMenu = new Menu("Bank");
		bankDeposit = new MenuItem("Make a Deposit");
		bankWithdraw = new MenuItem("Make a Withdrawal");
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
					tickerBox.append("You don't have that kind of money. \n");
					return;
				}
				cash -= spaceCost;
				maxSpace += 20;
				space += 20;
				tickerBox.append("You spend " + spaceCost + " bucks adding pockets, pouches, etc. \n");
				shopBuy10Space.setLabel("Buy 20 Space ($" + (maxSpace * 2) + ")");
				refreshStats();
			}	
		});
		loanPayBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (cash > debt) {
					tickerBox.append("With a look of surprise, the loan shark takes your money. \n");
					cash = cash - debt;
					debt = 0;
					tickerBox.append("    \"Pleasure doing business with you.\" \n");
					refreshStats();
				}
				else {
					tickerBox.append("When it becomes obvious you don't have the money, \n  he has a very large thug throw you out. \n");
				}
			}
		});
		loanTake.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				tickerBox.append("The Shark hands over another grand and makes a mark in his ledger. \n");
				cash += 1000;
				debt += 1000;
				tickerBox.append("    \"Tick, tock.\" \n");
				refreshStats();
			}
		});
		hospitalPartialHeal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (health == 100) {
					tickerBox.append("You're hale and hearty! \n");
				}
				else if (cash < 100) { // Heartless!
					tickerBox.append("The ER receptionist takes one look at you and has security throw you out. \n");
					tickerBox.append("    \"This ain't no free clinic.\" \n");
				}
				else if (dailyHeals <= 0) {
					tickerBox.append("You've been in too much; the staff recognize you from earlier today. \n");
					tickerBox.append("    \"Listen, man. You either check yourself in or you walk.\" \n");
				}
				else {
					cash = cash - 100;
					dailyHeals = dailyHeals - 1; // Limited heals per day
					health = (health + 10 >= 100) ? 100 : health + 10; // Max out at 100 health
					tickerBox.append("An EMT takes a C-note to give you some no-questions-asked attention. \n");
					refreshStats();
				}
			}
		});
		hospitalFullHeal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (health == 100) {
					tickerBox.append("You're hale and hearty! \n");
				}
				else if (cash < 1500) { // This is America
					tickerBox.append("The orderly tries to make you bounce as he throws you out the door. \n");
					tickerBox.append("    \"No insurance, no cash, no treatment.\" \n");
				}
				else {
					cash = cash - 1500;
					health = 100;
					tickerBox.append("You manage to convince the doctors not to report anything. You pay with cash. \n");
					endOfDay(false);
				}
			}
		});
		bankDeposit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (bank == 0 && cash >= 1000) { // New customer
					tickerBox.append("You find a bank that will take your money and open an account. \n");
					cash -= 1000;
					bank += 1000;
					tickerBox.append("    \"You don't ask if we're licensed and we don't ask where you got that money.\" \n");
				}
				else if (cash >= 1000) { // Return customer
					tickerBox.append("The guy behind the counter takes your money with a wink. \n");
					cash -= 1000;
					bank += 1000;
				}
				else { // Deadbeat
					tickerBox.append("This bank only takes deposits in thousand-dollar increments. \n");
				}
				
				refreshStats();
			}
		});
		bankWithdraw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (bank == 0) { // Deadbeat
					tickerBox.append("The clerk does not think you're very funny. \n");
				}
				else if (bank == 1000) { // Down on your luck
					tickerBox.append("You take your last grand out of the bank. \n");
					cash += 1000;
					bank = 0;
					tickerBox.append("    \"Tough times, huh?\" \n");
				}
				else { // Another satisfied customer
					tickerBox.append("You withdraw $1000. Some of the bills are moist, but the money's all there. \n");
					cash += 1000;
					bank -= 1000;
				}
				
				refreshStats();
			}
		});
		// Populate Menus
		mainWindowMenu.add(shopMenu);
		mainWindowMenu.add(loanMenu);
		shopMenu.add(shopBuyGun);
		shopMenu.add(shopBuy10Space);
		loanMenu.add(loanPayBack);
		loanMenu.add(loanTake);
		hospitalMenu.add(hospitalPartialHeal);
		hospitalMenu.add(hospitalFullHeal);
		bankMenu.add(bankDeposit);
		bankMenu.add(bankWithdraw);
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
		Panel statsPanel = createStatsPanel();
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
					
					if (!drugLocalPrices.containsKey(drugName)) {
						tickerBox.append("Nobody's buying that here.\n");
						return;
					}
					
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
					tickerBox.append("Selling " + drugName + " for $" + price + "\n");
					if (price > 2) {
						drugLocalPrices.put(drugName, price - 2);
					}
					else {
						drugLocalPrices.put(drugName, 1);
					}
					
					refreshStats();
					refreshBuyPrices();
					refreshSellPrices();
				}
				catch (Exception e) {
					StackTraceElement l = e.getStackTrace()[0];
					System.out.print("Exception raised while selling drugs: ");
					System.out.println(l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber());
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
						menuButtonRevise();
						break;
					}
				}
				endOfDay(false);
			}
		});
		jetDialog.add(jetChoices);
		jetDialog.add(jetChoicesOkButton);
		jetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jetDialog.setVisible(true);
				jetChoicesOkButton.requestFocusInWindow();
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
		drugLocalPrices.clear();
		for (int i = 0; i < drugs.length; i++) {
			// Should this drug be available at this location today?
			if (Math.random() < 0.2) { // ~ 4/5 chance of drug being at any given location
				continue;
			}
			
			// If so, how much should it cost?
			double priceFactor = getPriceFactor(drugs[i]);
			int finalPrice = (int)(drugBasePrices.get(drugs[i]) * priceFactor);
			if (finalPrice <= 0) { // No such thing as free drugs, kids.
				finalPrice = 1;
			}
			drugLocalPrices.put(drugs[i], finalPrice);
			buyPrices.add(drugs[i] + ": $" + finalPrice);
		}
		
		if (buyPrices.getItemCount() == 0) { // No drugs available!
			tickerBox.append("Cops are out in force today. Dealers and customers alike are laying low.");
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
			if (drugLocalPrices.containsKey(drugs[i])) {
				buyPrices.add(drugs[i] + ": $" + drugLocalPrices.get(drugs[i]));
			}
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
	private void endOfDay(boolean uneventfulDay) {
		day++;
		debt = (int)(debt * 1.1); // Will round down for floating points. He's a loan shark, but he's not Satan.
		dailyHeals = 3; // These refresh every day, as the name implies
		refreshStats();
		generateBuyPrices();
		if (day == 31) {
			endOfGame();
		}
		else if (!uneventfulDay) { // Possibly include random flavor event
			double eventSeed = Math.random();
			if (eventSeed < 0.01) {
				tickerBox.append("An old lady asks, \"Would you like a jelly baby?\" \n");
			}
			else if (eventSeed < 0.02) {
				tickerBox.append("A street preacher informs everyone that they are doomed. \n");
			}
			else if (eventSeed > 0.9) {
				attackerHP = 100;
				combatWindow("Officer Hardass", 10);
			}
		}
	}
	
	/**
	 * This ends the game.
	 */
	private void endOfGame() {
		Dialog endGameDialog = new Dialog(this, "Game Over!", true);
		endGameDialog.setLayout(new BorderLayout(3, 3));
		endGameDialog.setSize(300, 300);
		endGameDialog.setLocationRelativeTo(null);
		
		TextArea endGameText = new TextArea("", 10, 20, TextArea.SCROLLBARS_VERTICAL_ONLY);
		endGameText.setEditable(false);
		
		if (health > 0) {
			if (debt > 0) { // Time to pay up
				endGameText.append("On the morning of the 31st day, the Shark and his goons burst into your apartment. ");
				endGameText.append("After viciously beating you, they proceed to tear your place apart for your money. ");
				if (cash > debt) {
					cash = cash - debt;
					if (cash > 200) {
						cash = cash - 200;
						endGameText.append("They also take an extra $200 as a \"collection charge.\" ");
					}
					else {
						cash = 0;
						endGameText.append("They also take an the rest of your cash as a \"collection charge.\" ");
					}
					endGameText.append("One of the goons spits in your face as he leaves. \n \n");
				}
				else { // Oh, Paulie...won't see him no more.
					cash = 0;
					endGameText.append("When they don't find enough to pay your debt, the Shark sighs and motions toward the door. ");
					endGameText.append("The goons knock you out, and everything goes black. \n \n");
					health = 0;
				}
				
				debt = 0; // One way or another, you don't owe him any money, now.
			}
			else { // Very forward-thinking of you!
				endGameText.append("You paid off the Shark before your loan came due. Smart. \n \n");
			}
		}
		
		String aliveText = (health > 0) ? "YOU SURVIVED! \n" : "YOU DIED! \n";
		endGameText.append(aliveText);
		
		endGameText.append("You earned " + ((cash + bank) * 2) + " points!");
		
		endGameDialog.add(endGameText, BorderLayout.CENTER);
		
		Button endGameOkButton = new Button("Thanks for Playing!");
		endGameOkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				System.exit(0);  // Terminate the program
			}
		});
		endGameDialog.add(endGameOkButton, BorderLayout.SOUTH);
		
		endGameDialog.setVisible(true);
	}
	
	// TODO: Consider adding miniature stats panel in here.
	private void combatWindow(String attacker, int attackerBaseDamage) {
		Dialog combatDialog = new Dialog(this, "Combat!", true);
		combatDialog.setLayout(new BorderLayout(3, 3));
		combatDialog.setSize(300, 300);
		combatDialog.setLocationRelativeTo(null);
		
		TextArea combatText = new TextArea("", 5, 20, TextArea.SCROLLBARS_VERTICAL_ONLY);
		combatText.setEditable(false);
		combatDialog.add(combatText, BorderLayout.NORTH);
		
		combatText.append("You are detained by " + attacker + "! \n");
		
		Panel combatButtonsPanel = new Panel(new GridLayout(4, 1));
		
		Button surrenderCombatButton = new Button("Surrender");
		surrenderCombatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				tickerBox.append("You surrender. \n"); 
				if (space < maxSpace) { // You're carrying
					tickerBox.append(attacker + " confiscates all your drugs. \n");
					space = maxSpace;
					drugsInPocket.clear();
					pocketDrugPrices.clear();
					sellPrices.removeAll();
					tickerBox.append("They also confiscate your cash. \n");
					cash = 0;
					tickerBox.append("After 3 days, a bailiff throws you out to free up a cell. \n");
					 // In case I want to change how long you're held
					for(int i = 0; i < 2; i++) { // First day is today, so just 2 more
						endOfDay(true);
					}
				} 
				else { // I'm letting you off easy this time...
					tickerBox.append("The cops don't find any drugs on you. \n");
					tickerBox.append("    \"I'm watching you, punk.\" \n");
				}
				
				refreshStats();
				combatDialog.setVisible(false);
			}
		});
		
		Button runCombatButton = new Button("Run For It");
		runCombatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				combatText.append("You make a break for it! \n");
				double retreatSeed = Math.random();
				if (retreatSeed < 0.5) { // Escape!
					tickerBox.append("You manage to get away. \n");
					combatDialog.setVisible(false);
				}
				else { // Nope!
					// TODO: Abstract attack/defend away to prevent repeated code
					combatText.append("They're still hot on your tail! \n");
					combatText.append(attacker + " takes a shot! \n");
					double attackerHitSeed = Math.random();
					if (attackerHitSeed < 0.4 ) { // Attacker's not a crack shot
						combatText.append("The shot goes wide! \n");
					}
					else { // A hit! A very palpable hit!
						int damage = (int)(attackerBaseDamage * (Math.random() * 2));
						damage = damage > 1 ? damage : 1; // Do at least *some* damage!
						combatText.append("You are shot for " + damage + " HP! \n");
						health = health - damage;
						if (health <= 0) { // OH NOES!
							health = 0;
							combatDialog.setVisible(false);
							endOfGame();
						}
					}
				}
				refreshStats();
			}
		});
		
		Button attackCombatButton = new Button("Fight!");
		attackCombatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				double attackerHitSeed = Math.random();
				double yourHitSeed = Math.random();
				int attackerDamage = (int)(attackerBaseDamage * (Math.random() * 2));
				attackerDamage = attackerDamage > 1 ? attackerDamage : 1; // Do at least *some* damage!
				int yourDamage = (int)(playerGunBaseDamage * (Math.random() * 2));
				yourDamage = yourDamage > 1 ? yourDamage : 1; // Do at least *some* damage!
				if (!haveGun) { // Yeah, that'll go over great.
					yourDamage = 1;
					combatText.append("You flail wildly with your fist! \n");
					if (yourHitSeed < 0.2) { // You're trying to punch a cop, man.
						combatText.append("You manage to punch " + attacker + " for 1 HP! \n");
						attackerHP = attackerHP - 1;
					}
					else {
						combatText.append("You miss! How embarrassing! \n");
					}
				}
				else { // BANG!
					combatText.append("You take a shot with your gun! \n");
					if (yourHitSeed < 0.5) { 
						combatText.append("You manage to hit " + attacker + " for " + yourDamage + " HP! \n");
						attackerHP = attackerHP - yourDamage;
					}
					else {
						combatText.append("You miss! How embarrassing! \n");
					}
				}
				if (attackerHP <= 0) { // He's dead, Jim.
					refreshStats();
					tickerBox.append("You have heartlessly murdered " + attacker + "! \n");
					combatDialog.setVisible(false);
				}
				else { // He's alive, angry, and has a gun, Jim.
					combatText.append(attacker + " takes a shot! \n");
					if (attackerHitSeed < 0.4 ) { // Attacker's not a crack shot
						combatText.append("The shot goes wide! \n");
					}
					else { // A hit! A very palpable hit!
						int damage = (int)(attackerBaseDamage * (Math.random() * 2));
						damage = damage > 1 ? damage : 1; // Do at least *some* damage!
						combatText.append("You are shot for " + damage + " HP! \n");
						health = health - damage;
						if (health <= 0) { // OH NOES!
							health = 0;
							combatDialog.setVisible(false);
							endOfGame();
						}
					}
				}
				refreshStats();
			}
		});
		
		// Add buttons to panel
		combatButtonsPanel.add(attackCombatButton);
		combatButtonsPanel.add(surrenderCombatButton);
		combatButtonsPanel.add(runCombatButton);

		combatDialog.add(combatButtonsPanel, BorderLayout.SOUTH);
		combatDialog.setVisible(true);
	}
	
	/**
	 * This method creates the stats panel at the top of the CrankWarsWindow
	 * <p>
	 * Update that window using refreshstats.
	 */
	private Panel createStatsPanel() {
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
		
		return statsPanel;
	}

	/**
	 * This method creates a price factor for a given drug, and activates special events
	 * at pseudorandom intervals
	 */
	private double getPriceFactor(String drugName) {
		double eventSeed = Math.random();
		int whichEvent = new java.util.Random().nextInt(3);
		if (eventSeed < 0.05) {
			switch (whichEvent) { // Why's the price this way?
				case 0:
					tickerBox.append("An influx of cheap " + drugName + " is driving down prices! \n");
					break;
				case 1:
					tickerBox.append("Rival dealers flood the " + drugName + " market! Prices are low! \n");
					break;
				case 2:
					tickerBox.append("Prices low after a huge shipment of " + drugName + " slipped past authorities! \n");
					break;
				default: // Something is screwy, but we still need event text
					tickerBox.append(drugName + ". " + drugName + " everywhere. \n");
					break;
			}
			
			// And the change in price is what?
			return Math.random() + eventSeed;
		}
		else if (eventSeed < 0.1) {
			switch (whichEvent) { // Why's the price this way?
				case 0:
					tickerBox.append("Cops bust local " + drugName + " dealers! Prices are insane! \n");
					break;
				case 1:
					tickerBox.append("Addicts are buying " + drugName + " at ridiculous prices! \n");
					break;
				case 2:
					tickerBox.append("Everybody's itching for  " + drugName + "! Prices are higher than the junkies! \n");
					break;
				default: // Something is screwy, but we still need event text
					tickerBox.append("Seems like nobody's got any " + drugName + ". People would pay top dollar for it. \n");
					break;
			}
			
			// And the change in price is what?
			return 2 + eventSeed;
		}
		else if (drugName == "Weed" && eventSeed < 0.15) {
			tickerBox.append("The Marrakesh Express has arrived! \n");
			return 0.15;
		}
		
		// No events for this drug today:
		return (Math.random() * 1.8) + 0.5;
	}
	
	/**
	 * This method revises available menu options when you travel
	 */
	 private void menuButtonRevise() {
		 // Handle loan shark menu
		 mainWindowMenu.remove(loanMenu);
		 mainWindowMenu.remove(hospitalMenu);
		 mainWindowMenu.remove(bankMenu);
		 loanMenu.removeAll();
		 if (curLocation == 0 && day < 21) { // Shark's office is in Manhattan
			 mainWindowMenu.add(loanMenu);
			 if (debt > 0) { // Can't pay back loans you don't have!
				loanMenu.add(loanPayBack);
			 }
			 loanMenu.add(loanTake);
		 }
		 else if (curLocation == 1 || curLocation == 4) { // There are two hospitals, because reasons
			 mainWindowMenu.add(hospitalMenu);
		 }
		 else if (curLocation == 2) { // The kind of bank that serves your demographic doesn't have ATMs
			 mainWindowMenu.add(bankMenu);
		 }
	 }
}

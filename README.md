# EasyTrain

EasyTrain is a Java Application that talks to the Uhlenbrock Intellibox via p50x commands to control model trains. The trains are controlled on a layout that can be modelled using EasyTrains user interface. 

Easy Train allows the user to drive trains manually from a start block to a destination block. The route in between the blocks is determined by EasyTrains automatic routing feature.

The second mode of operatation besides the manual mode is fully automated mode. In fully automated mode, trains will start from their initial block, randomly select target blocks to drive to and then loop back to their initial block from where they start their random routing over again. The automated mode allows the show-casing of your layout without manual planning of routes.

## Usage

Use the up and down arrow keys to zoom in and out.

When EasyTrain is opened, it trys to connect to the IntelliBox and outputs a message if the connection failed. Without connection, simlated operation is possible.

If a connection the Intellibox is established, EasyTrain will read the status of all blocks from the Intellibox. Reading the status, EasyTrain learns which blocks are used. It does not know yet which block is used by which train. Therefore you have to add your trains into the train list manually while inserting the trains address. Then assign the trains manually to the blocks that they are actually located on.

When EasyTrain knows which blocks are used and when it knows the address of the trains on those blocks, it can compute routes and send the appropriate commands to drive the trains from block to block.

The algorithm will select three blocks at random, drive the locomotive over each of these three blocks and finally it will return the locomotive to the original block that the locomotive started from.

There are currently the following constraints:
- EasyTrain only drives trains in forward direction. A train will never drive in reverse direction.
- Starting trains from dead end rails will not work properly (it will work for one cycle only) because the train will enter the dead end in reverse orientation that it started from. Because Easy Train does not ever drive trains in reverse but only heading forward, a train that is headed towards the dead end of a rail will never be able to drive again.
- Because of the train above, only start trains from blocks that are part of loops.

### Placing a Train On a Block

- Left-Mouse-Click a block to select it. Blocks are rails that have a number to them. The number is the block's address.
- Select Edit > Place Locomotive from the Menu Bar.
- Select one train from the list. To select a train click into the row of the train but do not click on the trains picture.
- Click one of the direction buttons (North, East, South, West) to determine the direction in which the train is facing on the rail so that Easy Train knows if it has to send forwards or backwards commands to operate the train.
- Click Ok to submit your selection.
- The train is now assigned to the block.
- You can repeate the process to assign several trains to several blocks.
- Per block you can only assign a single train.

### Simulated Operation

During Simulated operation, there is no connection to the Intellibox and no commands are sent. Instead trains traverse the layout in simulation. 

To run a simulation

- Place one or more trains on blocks.
- From the MenuBar, select Routing Controller > Start. The Routing controller will now run. 
- Since the operation of the entire application is paused, you have to unpause the application to see the results.
- To unpause the application, use the "Toggle Pause" button which is located at the bottom of the screen.
- The simulation will now select blocks to travel to, and it will paint the layout according to the simulated trains progress along it's route.


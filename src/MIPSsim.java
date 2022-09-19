//
//	On my honor, I have neither given nor received unauthorized aid on this assignment
//
//	Name: Aravind S
//	Project 2 - MIPS Simulation with Pipeline
//
//
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class MIPSsim {

    private static boolean isWaiting = false;			//True = Waiting || False = Executed
    private static boolean isExecuted = false;
    private static String instructionFetch = null;

    private static ArrayList<String> buf1 = new ArrayList<String>(8);
    private static ArrayList<String> buf2 = new ArrayList<String>(2);
    private static ArrayList<String> buf3 = new ArrayList<String>(2);
    private static ArrayList<String> buf4 = new ArrayList<String>(2);
    private static String buf5 = null;
    private static String buf6 = null;
    private static String buf7 = null;
    private static String buf8 = null;
    private static String buf9 = null;
    private static String buf10 = null;
    private static String  loadMemory= null;

    private static ArrayList<String> writeBack = new ArrayList<String>(8);
    private static HashSet<Integer> registerWrite = new HashSet<Integer>();
    private static HashSet<Integer> registerRead = new HashSet<Integer>();
    private static HashSet<Integer> registerOrder = new HashSet<Integer>();

    private static boolean instructionFlag = true;
    private static int cycles = 0;
    private static int dataCounter = -1;
    private static int nextPosCounter = 256;		// For keeping track of branch instructions
    private static int[] regArray = new int[32];
    private static HashMap<Integer, String> hm = new HashMap<Integer, String>();
    private static HashMap<Integer, Integer> dataMap =  new HashMap<Integer, Integer>();

    public static void main(String args[]){
        long data;
        String nextInstr = null;
        String fileName = args[0];
        File file = new File("");
        String currentDirectory = file.getAbsolutePath();
        BufferedReader br = null;
        try {
            //********* Create output files *********
            File disassemblyFile = new File("disassembly.txt");
            File simulationFile = new File("simulation.txt");
            if (!disassemblyFile.exists()) {
                disassemblyFile.createNewFile();
            }
            if (!simulationFile.exists()) {
                simulationFile.createNewFile();
            }
            br = new BufferedReader(new FileReader(currentDirectory + "/" + fileName));
            FileWriter fw1 = new FileWriter(disassemblyFile.getAbsoluteFile());
            BufferedWriter bw1 = new BufferedWriter(fw1);

            //********* Save data and instructions in HashMap *********

            int posCntr = 260;
            while((nextInstr = br.readLine()) != null){
                hm.put(posCntr, nextInstr);
                posCntr += 4;
            }
            posCntr = 260;


            //********* Disassemble the input file (for disassembly.txt output) *********


            while(hm.containsKey(posCntr)){
                nextInstr = hm.get(posCntr);
                if(instructionFlag){
                    bw1.write(nextInstr + "\t" + posCntr + "\t" + disassembleInstruction(nextInstr) + "\n");
                    if (nextInstr.compareTo("00011000000000000000000000000000") == 0)
                        instructionFlag = false;
                }
                else {
                    //********* Read input data *********
                    if (dataCounter == -1)
                        dataCounter = posCntr;
                    data = Long.parseLong(nextInstr, 2);
                    if (data > 2147483647)
                        data = data - 4294967296l;
                    dataMap.put(posCntr, (int) data);
                    bw1.write(nextInstr +"\t" + posCntr +"\t" + data + "\n");
                }
                posCntr += 4;
            }
            bw1.close();


            //********* Simulate instruction execution (for simulation.txt output) *********


            int ifCount = 0;
            FileWriter fw2 = new FileWriter(simulationFile.getAbsoluteFile());
            BufferedWriter bw2 = new BufferedWriter(fw2);
            nextPosCounter = posCntr = 260;
            //**************** Start Execution Cycle ****************
            while(nextPosCounter < dataCounter){
                boolean buf2Full = false;
                boolean buf3Full = false;
                boolean buf4Full = false;
                registerOrder.clear();

                if(buf8 != null){
                    loadMemory = buf8;
                    buf8 = null;
                }
                if(loadMemory != null){
                    writeBack.add(loadMemory);
                    loadMemory = null;
                }

                if(buf5 != null){
                    //System.out.println("Load/Store:: " + disassembleInstruction(buf6) + "\t" + buf6);
                    if(buf5.startsWith("000100")){
                        //System.out.println("Store:: " + disassembleInstruction(buf6) + "\t" + buf6);
                        writeBack.add(buf5);
                    }
                    else if (buf5.startsWith("000101")){
                        //System.out.println("Load:: " + disassembleInstruction(buf6) + "\t" + buf6);
                        buf8 = buf5;
                    }
                    buf5 = null;
                }

                if(buf6 != null){
                    writeBack.add(buf6);
                    buf6 = null;
                }
                if(buf10 != null){
                    writeBack.add(buf10);
                    buf10 = null;
                }
                if(buf9 != null){
                    buf10 = buf9;
                    buf9 = null;
                }
                if(buf7 != null){
                    buf9 = buf7;
                    buf7 = null;
                }

                if(!buf4.isEmpty()){
                    if(buf4.size() == 2)
                        buf4Full = true;
                    buf7 = buf4.get(0);
                    buf4.remove(0);
                }
                if(!buf3.isEmpty()){
                    if(buf3.size() == 2)
                        buf3Full = true;
                    buf6 = buf3.get(0);
                    buf3.remove(0);
                }
                if(!buf2.isEmpty()){
                    if(buf2.size() == 2)
                        buf2Full = true;
                    buf5 = buf2.get(0);
                    buf2.remove(0);
                }
                if(!buf1.isEmpty()) {
                    Iterator<String> iter = buf1.iterator();
                    while (iter.hasNext()) {
                        String bufInstruction = iter.next();
                        String instructionType = bufInstruction.substring(0, 6);
                        // ALU1 Unit
                        if (instructionType.equals("000100") || instructionType.equals("000101")) {
                            if (!buf2Full && buf2.size() < 2 && executeInstruction(bufInstruction, false)) {
                                buf2.add(bufInstruction);
                                iter.remove();
                            }
                        }
                        //MUL
                        else if (instructionType.equals("001110")) {
                            if (!buf4Full && buf4.size() < 2 && executeInstruction(bufInstruction, false)) {
                                buf4.add(bufInstruction);
                                iter.remove();
                            }
                        }
                        //ALU2
                        else{
                            if(!buf3Full && buf3.size() < 2 && executeInstruction(bufInstruction, false)){
                                buf3.add(bufInstruction);
                                iter.remove();
                            }
                        }
                    }
                }

                if(instructionFetch != null && instructionFetch.substring(0, 6).equals("000000")){
                    //System.out.println("JUMP" + posCntr);
                    instructionFetch = null;
                    isExecuted = false;
                    isWaiting = false;
                }
                if(instructionFetch != null){

                    HashSet<Integer> buf1Write = new HashSet<Integer>();
                    if(!buf1.isEmpty()) {
                        Iterator<String> iter = buf1.iterator();
                        while (iter.hasNext()) {
                            String bufInstruction = iter.next();
                            String category = bufInstruction.substring(0, 3);
                            String opCode = bufInstruction.substring(3, 6);
                            String bits = bufInstruction.substring(6);
                            switch (category) {
                                case "000":
                                    if (opCode == "101") {
                                        int src1 = Integer.parseInt(bits.substring(0, 5), 2);
                                        buf1Write.add(src1);
                                    }
                                    break;
                                case "010":
                                case "001":
                                    int dest = Integer.parseInt(bufInstruction.substring(6, 11), 2);
                                    buf1Write.add(dest);
                                    break;

                            }

                        }
                    }

                        posCntr = processCategoryJumps(posCntr, instructionFetch, buf1Write);
                        if(posCntr == -1){
                            posCntr = nextPosCounter;
                            isWaiting = true;
                        }
                        else {
                            if(isExecuted){
                                isWaiting = false;
                                instructionFetch = null;
                                if (posCntr == nextPosCounter)
                                    nextPosCounter += 4;
                                else
                                    nextPosCounter = posCntr;
                            }
                            else {
                                posCntr = nextPosCounter;
                                isExecuted = true;
                            }
                        }

                }
                ifCount = 0;
                while(ifCount < 4 && buf1.size() < 8 && isWaiting == false){
                    posCntr = nextPosCounter;
                    nextInstr = hm.get(posCntr);
                    if(nextInstr.compareTo("00011000000000000000000000000000") == 0){
                        instructionFetch = nextInstr;
                        isWaiting = true;
                        isExecuted = true;
                        nextPosCounter = dataCounter;
                        break;
                    }
                    if(nextInstr.substring(0, 4).equals("0000")){
                        instructionFetch = nextInstr;
                        isWaiting = true;
                        if(instructionFetch.substring(0, 6).equals("000000")){
                            //System.out.println("JUMP");
                            nextPosCounter = processCategoryJumps(posCntr, instructionFetch, new HashSet<>());
                            isExecuted = true;
                        }
                        else
                            isExecuted = false;
                        break;
                    }
                    buf1.add(nextInstr);
                    ifCount++;
                    if (posCntr == nextPosCounter)
                        nextPosCounter += 4;
                }
                if(!writeBack.isEmpty()){
                    Iterator<String> wb = writeBack.iterator();
                    while(wb.hasNext()){
                        String s = wb.next();
                        executeInstruction(s, true);
                        wb.remove();
                    }
                }
                //**************** End Execution Cycle ****************
                //System.out.println(posCntr + "\t\t" + nextInstr + "\t\t" + disassembleInstruction(nextInstr));
                printSimulationState(bw2);
            }
            bw2.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                if (br!=null)
                    br.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        // END of main function
    }


    //****************************** Execute Instructions ******************************
    private static boolean executeInstruction(String nextInstr, boolean execute) {
        // TODO Auto-generated method stub
        String instrCategory = nextInstr.substring(0, 3);
        switch (instrCategory) {
            case "000":		//********* Category 1
                return processCategoryOne(nextInstr, execute);
            case "001":		//********* Category 2
                return processCategoryTwo(nextInstr, execute);
            case "010":		//********* Category 3
                return processCategoryThree(nextInstr, execute);
            default:		//********* Input Error
                //********* should not execute (errors not handled) *********
                System.out.println(nextInstr + " : Invalid Instruction");
                return false;	//Exit program
        }
    }


    //****************************** Category Jumps ******************************
    private static int processCategoryJumps(int posCntr, String mipsInstr, HashSet<Integer> buf1Write) {
        // Format of Instructions in Category-1
        String opCode = mipsInstr.substring(3, 6);
        String bits = mipsInstr.substring(6);
        int src1 = Integer.parseInt(bits.substring(0, 5), 2);
        int src2 = Integer.parseInt(bits.substring(5, 10), 2);
        int offset = Integer.parseInt(bits.substring(10), 2) << 2;
        switch (opCode) {
            case "000":
                // J	[JUMP]
                posCntr = (posCntr+4) & 0xf0000000;
                posCntr |= Integer.parseInt(bits, 2) << 2;
                break;
            case "001":
                // BEQ
                if(registerWrite.contains(src1) || registerWrite.contains(src2) || buf1Write.contains(src1) || buf1Write.contains(src2))
                    return -1;
                if (regArray[src1] == regArray[src2]){
                    posCntr = posCntr + 4 + offset;
                }
                break;
            case "010":
                // BNE
                if(registerWrite.contains(src1) || registerWrite.contains(src2) || buf1Write.contains(src1) || buf1Write.contains(src2))
                    return -1;
                if (regArray[src1] != regArray[src2]){
                    posCntr = posCntr + 4 + offset;
                }
                break;
            case "011":
                // BGTZ
                if(registerWrite.contains(src1) || buf1Write.contains(src1))
                    return -1;
                if (regArray[src1] > 0){
                    posCntr = posCntr + 4 + offset;
                }
                break;
        }
        return posCntr;
    }


    //****************************** Category One ******************************
    private static boolean processCategoryOne(String mipsInstr, boolean execute) {
        // Format of Instructions in Category-1
        String opCode = mipsInstr.substring(3, 6);
        String bits = mipsInstr.substring(6);
        //int data = 0;
        int src1 = Integer.parseInt(bits.substring(0, 5), 2);
        int src2 = Integer.parseInt(bits.substring(5, 10), 2);
        int offset = Integer.parseInt(bits.substring(10), 2) << 2;
        switch (opCode) {
            case "100":
                // SW
                if (execute){
                    dataMap.put((offset >> 2) + regArray[src1], regArray[src2]);
                    registerRead.remove(src1);
                    registerRead.remove(src2);
                }
                else {
                    registerOrder.add(src1);
                    registerOrder.add(src2);
                    if (registerWrite.contains(src1) || registerWrite.contains(src2))
                        return false;
                    registerRead.add(src1);
                    registerRead.add(src2);
                }
                break;
            case "101":
                // LW
                if (execute){
                    regArray[src2] = dataMap.get((offset >> 2)+ regArray[src1]);
                    //System.out.println("Load:: "+ mipsInstr + "\t" + src2 + "\t" + regArray[src2] + "\t" + ((offset >> 2) + regArray[src1]));
                    registerRead.remove(src1);
                    registerWrite.remove(src2);
                    int a = 1+2;
                }
                else {
                    if (registerRead.contains(src2) || registerWrite.contains(src2))
                        return false;
                    registerRead.add(src1);
                    registerWrite.add(src2);
                }
                break;
        }
        return true;
    }


    //****************************** Category Two ******************************
    private static boolean processCategoryTwo(String mipsInstr, boolean execute) {
        // Format of Instructions in Category-2
        String opCode = mipsInstr.substring(3, 6);
        int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
        int src1 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
        int src2 = Integer.parseInt(mipsInstr.substring(16, 21), 2);
        switch (opCode) {
            case "000":
                //ADD
                if (execute){
                    regArray[dest] = regArray[src1] + regArray[src2];
                    registerRead.remove(src1);
                    registerRead.remove(src2);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) || registerWrite.contains(src2) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerRead.add(src2);
                    registerWrite.add(dest);
                }
                break;
            case "001":
                //SUB
                if (execute){
                    regArray[dest] = regArray[src1] - regArray[src2];
                    registerRead.remove(src1);
                    registerRead.remove(src2);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) || registerWrite.contains(src2) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerRead.add(src2);
                    registerWrite.add(dest);
                }
                break;
            case "010":
                //AND
                if (execute){
                    regArray[dest] = regArray[src1] & regArray[src2];
                    registerRead.remove(src1);
                    registerRead.remove(src2);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) || registerWrite.contains(src2) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerRead.add(src2);
                    registerWrite.add(dest);
                }
                break;
            case "011":
                //OR
                if (execute){
                    regArray[dest] = regArray[src1] | regArray[src2];
                    registerRead.remove(src1);
                    registerRead.remove(src2);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) || registerWrite.contains(src2) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerRead.add(src2);
                    registerWrite.add(dest);
                }
                break;
            case "100":
                //SRL
                if (execute){
                    regArray[dest] = regArray[src1] >>> 2;
                    registerRead.remove(src1);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerWrite.add(dest);
                }
                break;
            case "101":
                //SRA
                if (execute){
                    regArray[dest] = regArray[src1] >> 2;
                    registerRead.remove(src1);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerWrite.add(dest);
                }
                break;
            case "110":
                //MUL
                if (execute){
                    regArray[dest] = regArray[src1] * regArray[src2];
                    registerRead.remove(src1);
                    registerRead.remove(src2);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) || registerWrite.contains(src2) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerRead.add(src2);
                    registerWrite.add(dest);
                }

        }
        return true;
    }


    //****************************** Category Three ******************************
    private static boolean processCategoryThree(String mipsInstr, boolean execute) {
        // Format of Instructions in Category-3
        String opCode = mipsInstr.substring(3, 6);
        int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
        int src1 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
        int data = Integer.parseInt(mipsInstr.substring(16), 2);
        switch (opCode) {
            case "000":
                //ADDI
                if (execute){
                    regArray[dest] = regArray[src1] + data;
                    registerRead.remove(src1);
                    registerWrite.remove(dest);
                }
                else {
                    if(registerOrder.contains(src1) || registerOrder.contains(dest))
                        return false;
                    if (registerWrite.contains(src1) || registerWrite.contains(dest) )//|| registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerWrite.add(dest);
                }
                break;
            case "001":
                //ANDI
                if (execute){
                    regArray[dest] = regArray[src1] & data;
                    registerRead.remove(src1);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerWrite.add(dest);
                }
                break;
            case "010":
                //ORI
                if (execute){
                    regArray[dest] = regArray[src1] | data;
                    registerRead.remove(src1);
                    registerWrite.remove(dest);
                }
                else {
                    if (registerWrite.contains(src1) ||
                            registerWrite.contains(dest) || registerRead.contains(dest))
                        return false;
                    registerRead.add(src1);
                    registerWrite.add(dest);
                }
                break;
        }
        return true;
    }
//****************************** Print the simulation.txt output file *******************************


    //****************************** Print Simulation State ******************************
    private static void printSimulationState(BufferedWriter bw) {
        cycles++;
        //if(cycle > 100)
        //	System.exit(0);
        //System.out.println("Cycle:  " + cycle);
        try {
            bw.write("--------------------");
            bw.write("\nCycle " + cycles + ":");
            bw.write("\n\nIF:");
            if(!isWaiting){
                bw.write("\n\tWaiting:");
                bw.write("\n\tExecuted:");
            }
            else {
                if (isExecuted){
                    bw.write("\n\tWaiting:");
                    bw.write("\n\tExecuted: [" + disassembleInstruction(instructionFetch) + "]");
                }
                else {
                    bw.write("\n\tWaiting: [" + disassembleInstruction(instructionFetch) + "]");
                    bw.write("\n\tExecuted:");

                }
            }
            //******************************
            bw.write("\nBuf1:");
            int loop = 0;
            for(loop=0; loop<buf1.size(); loop++){
                bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf1.get(loop)) + "]");
            }
            while(loop<8){
                bw.write("\n\tEntry " + loop++ + ":");
            }
            //******************************
            bw.write("\nBuf2:");
            for(loop=0; loop<buf2.size(); loop++){
                bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf2.get(loop)) + "]");
            }
            while(loop<2){
                bw.write("\n\tEntry " + loop++ + ":");
            }
            //******************************
            bw.write("\nBuf3:");
            int loop2 = 0;
            for(loop=0; loop<buf3.size(); loop++){
                bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf3.get(loop)) + "]");
            }
            while(loop<2){
                bw.write("\n\tEntry " + loop++ + ":");
            }
            //******************************
            bw.write("\nBuf4:");
            for(loop=0; loop<buf4.size(); loop++){
                bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf4.get(loop)) + "]");
            }
            while(loop<2){
                bw.write("\n\tEntry " + loop++ + ":");
            }
            //******************************
            /*bw.write("\nBuf5:");
            for(loop=0; loop<buf5.size(); loop++){
                bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf5.get(loop)) + "]");
            }
            while(loop<2){
                bw.write("\n\tEntry " + loop++ + ":");
            }*/
            bw.write("\nBuf5:");
            if(buf5 != null)
                bw.write(" [" + disassembleInstruction(buf5) + "]");
            bw.write("\nBuf6:");
            if(buf6 != null){
                bw.write(" [" + printBuf6(buf6) + "]");
            }
            bw.write("\nBuf7:");
            if(buf7 != null)
                bw.write(" [" + disassembleInstruction(buf7) + "]");
            /*if(buf7 != null){
                int src1 = Integer.parseInt(buf12.substring(6, 11), 2);
                int src2 = Integer.parseInt(buf12.substring(11, 16), 2);
                bw.write(" [" + (regArray[src1] % regArray[src2]) + ", " + (regArray[src1] / regArray[src2]) + "]");
            }
            bw.write("\nBuf8:");
            if(buf8 != null)
                bw.write(" [" + disassembleInstruction(buf8) + "]");*/
            bw.write("\nBuf8:");
            if(buf8 != null){
                String bits = buf8.substring(6);
                int src1 = Integer.parseInt(bits.substring(0, 5), 2);
                int src2 = Integer.parseInt(bits.substring(5, 10), 2);
                int offset = Integer.parseInt(bits.substring(10), 2) << 2;
                regArray[src2] = dataMap.get((offset >> 2)+ regArray[src1]);
                bw.write(" [" + dataMap.get((offset >> 2)+ regArray[src1])+ ", R" + src2 + "]");
            }
            bw.write("\nBuf9:");
            if(buf9 != null)
                bw.write(" [" + disassembleInstruction(buf9) + "]");
            bw.write("\nBuf10:");
            if(buf10 != null){
                int src1 = Integer.parseInt(buf10.substring(6, 11), 2);
                int src2 = Integer.parseInt(buf10.substring(11, 16), 2);
                bw.write(" [" + (regArray[src1] * regArray[src2]) + "]");
            }

            bw.write("\n\nRegisters");
            for(int i=0; i<32; i++){
                if (i%8 == 0){
                    bw.write("\n");
                    bw.write(String.format("R%02d:", i));
                }
                bw.write("\t" + regArray[i]);
            }

            bw.write("\n\n");
            bw.write("Data");
            int dataReg = dataCounter;
            for(int i=0; i<16; i++){
                if (i%8 == 0){
                    bw.write("\n");
                    bw.write(String.format("%03d:", dataReg));
                }
                bw.write("\t" + dataMap.get(dataReg));
                dataReg += 4;
            }

            bw.write("\n");
            bw.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    //****************************** Print Buffer 6 State ******************************
    private static String printBuf6(String buf6) {
        String result = "";
        if(buf6.startsWith("001")){
            String opCode = buf6.substring(3, 6);
            int dest = Integer.parseInt(buf6.substring(6, 11), 2);
            int src1 = Integer.parseInt(buf6.substring(11, 16), 2);
            int src2 = Integer.parseInt(buf6.substring(16, 21), 2);
            switch (opCode) {
                case "000":
                    //ADD
                    result += (regArray[src1] + regArray[src2]);
                    break;
                case "001":
                    //SUB
                    result += (regArray[src1] - regArray[src2]);
                    break;
                case "010":
                    //AND
                    result += (regArray[src1] & regArray[src2]);
                    break;
                case "011":
                    //OR
                    result += (regArray[src1] | regArray[src2]);
                    break;
                case "100":
                    //SRL
                    result += (regArray[src1] >>> 2);
                    break;
                case "101":
                    //SRA
                    result += (regArray[src1] >> 2);
                    break;
            }
            result += ", R" + dest;
        }
        else if(buf6.startsWith("010")) {
            String opCode = buf6.substring(3, 6);
            int dest = Integer.parseInt(buf6.substring(6, 11), 2);
            int src1 = Integer.parseInt(buf6.substring(11, 16), 2);
            int data = Integer.parseInt(buf6.substring(16), 2);
            switch (opCode) {
                case "000":
                    //ADDI
                    result += (regArray[src1] + data);
                    break;
                case "001":
                    //ANDI
                    result += (regArray[src1] & data);
                    break;
                case "010":
                    //ORI
                    result += (regArray[src1] | data);
                    break;
            }
            result += ", R" + dest;
        }

        return result;
    }


    //****************************** Print the disassembly.txt output file ******************************
    private static String disassembleInstruction(String mipsInstr) {
        String instructionCategory = mipsInstr.substring(0, 3);
        String opCode = mipsInstr.substring(3, 6);
        String instructionName = "";

        String destination;
        String source1;
        String source2;
        String data;
        String bits;
        int offset;

        switch (instructionCategory) {
            case "000":		//Category 1
                bits = mipsInstr.substring(6);
                source1 = "R" + Integer.parseInt(bits.substring(0, 5), 2);
                source2 = "R" + Integer.parseInt(bits.substring(5, 10), 2);
                offset = Integer.parseInt(bits.substring(10), 2) << 2;
                switch (opCode) {
                    case "000":
                        instructionName = "J ";
                        int temp = (nextPosCounter+4) & 0xf0000000;
                        temp |= Integer.parseInt(bits, 2) << 2;
                        instructionName += "#" + temp;
                        break;
                    case "001":
                        instructionName = "BEQ ";
                        instructionName += source1 + ", " + source2 + ", #" + offset;
                        break;
                    case "010":
                        instructionName = "BNE ";
                        instructionName += source1 + ", " + source2 + ", #" + offset;
                        break;
                    case "011":
                        instructionName = "BGTZ ";
                        instructionName += source1 + ", #" + offset;
                        break;
                    case "100":
                        instructionName = "SW ";
                        instructionName += source2 + ", " + (offset >> 2) + "(" + source1 + ")";
                        break;
                    case "101":
                        instructionName = "LW ";
                        instructionName += source2 + ", " + (offset >> 2)+ "(" + source1 + ")";
                        break;
                    case "110":
                        instructionName = "BREAK";
                        instructionFlag = false;
                        break;
                }
                break;
            case "001":		//Category 2
                destination = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
                source1 = "R" + Integer.parseInt(mipsInstr.substring(11, 16), 2);
                source2 = "R" + Integer.parseInt(mipsInstr.substring(16, 21), 2);
                bits = mipsInstr.substring(21);
                switch (opCode) {
                    case "000":
                        instructionName += "ADD ";
                        break;
                    case "001":
                        instructionName += "SUB ";
                        break;
                    case "010":
                        instructionName += "AND ";
                        break;
                    case "011":
                        instructionName += "OR ";
                        break;
                    case "100":
                        instructionName += "SRL ";
                        source2 = "" + Integer.parseInt(mipsInstr.substring(16, 21), 2);
                        instructionName += destination + ", " + source1 + ", #" + source2;
                        return instructionName;
                    case "101":
                        instructionName += "SRA ";
                        source2 = "" + Integer.parseInt(mipsInstr.substring(16, 21), 2);
                        instructionName += destination + ", " + source1 + ", #" + source2;
                        return instructionName;
                    case "110":
                        instructionName += "MUL ";
                        break;
                }
                instructionName += destination + ", " + source1 + ", " + source2;
                break;
            case "010":		//Category 3
                destination = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
                source1 = "R" + Integer.parseInt(mipsInstr.substring(11, 16), 2);
                data = "#" + Integer.parseInt(mipsInstr.substring(16), 2);
                switch (opCode) {
                    case "000":
                        instructionName = "ADDI ";
                        break;
                    case "001":
                        instructionName = "ANDI ";
                        break;
                    case "010":
                        instructionName = "ORI ";
                        break;
                }
                instructionName += destination + ", " + source1 + ", " + data;
                break;

        }
        return instructionName;
    }

//END PROGRAM
}

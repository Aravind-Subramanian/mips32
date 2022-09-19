//
//	On my honor, I have neither given nor received unauthorized aid on this assignment
//	Name: Srinivas Nishant Viswanadha
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
public class MIPS {
    private static int lowly = 0;
    private static int highly = 0;
    private static boolean lowlyused = false;
    private static boolean highlyused = false;
    private static boolean mulIssue = false;
    private static boolean divisionIssued = false;
    private static boolean itswaiting = false;			//True = Waiting || False = Executed
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
    private static String buf11 = null;
    private static String buf12 = null;
    private static String divisionInstr = null;
    private static String  loadmem= null;
    //private static boolean[] clearBuf = new boolean[13];

    private static ArrayList<String> WrBack = new ArrayList<String>(8);
    private static HashSet<Integer> WriteReg = new HashSet<Integer>();
    private static HashSet<Integer> ReadReg = new HashSet<Integer>();
    private static HashSet<Integer> OrderReg = new HashSet<Integer>();

    private static boolean instructionFlag = true;
    private static int cyls = 0;
    private static int dataCounter = -1;
    private static int nextPositionCnt = 256;		// For keeping track of branch instructions
    private static int[] ArrayRegister = new int[32];
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
            //*** Create output files ***
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

            //*** Save data and instructions in HashMap ***

            int posCntr = 260;
            while((nextInstr = br.readLine()) != null){
                hm.put(posCntr, nextInstr);
                posCntr += 4;
            }
            posCntr = 260;


            //*** Disassemble the input file (for disassembly.txt output) ***


            while(hm.containsKey(posCntr)){
                nextInstr = hm.get(posCntr);
                if(instructionFlag){
                    if (nextInstr.compareTo("00011000000000000000000000000000") == 0)
                        instructionFlag = false;
                }
                else {
                    //*** Read input data ***
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


            //*** Simulate instruction execution (for simulation.txt output) ***


            int divCount = 0;
            int ifCount = 0;
            FileWriter fw2 = new FileWriter(simulationFile.getAbsoluteFile());
            BufferedWriter bw2 = new BufferedWriter(fw2);
            nextPositionCnt = posCntr = 260;
//            instructionFlag = true;
            int checkCount = 0;
            //****** Start Execution Cycle ******
            while(nextPositionCnt < dataCounter){
                boolean buf2Full = false;
                boolean buf3Full = false;
                boolean buf4Full = false;
                //boolean bufFull5 = false;
                OrderReg.clear();


                if(buf8 != null){
                    loadmem = buf8;
                    buf8 = null;
                }
                if(loadmem != null){
                    WrBack.add(loadmem);
                    loadmem = null;
                }

                if(buf5 != null){
                    //System.out.println("Load/Store:: " + disassembleInstruction(buf6) + "\t" + buf6);
                    if(buf5.startsWith("000100")){
                        //System.out.println("Store:: " + disassembleInstruction(buf6) + "\t" + buf6);
                        WrBack.add(buf5);
                    }
                    else if (buf5.startsWith("000101")){
                        //System.out.println("Load:: " + disassembleInstruction(buf6) + "\t" + buf6);
                        buf8 = buf5;
                    }
                    buf5 = null;
                }




                if(buf6 != null){
                    WrBack.add(buf6);
                    buf6 = null;
                }
                if(buf10 != null){
                    WrBack.add(buf10);
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
                /*if(buf12 != null){
                    WrBack.add(buf12);
                    lowUsed = false;
                    buf12 = null;
                }
                if(buf11 != null){
                    buf12 = buf11;
                    buf11 = null;
                }
                if(buf10 != null){
                    loadMemory = buf10;
                    buf10 = null;
                }
                if(loadMemory != null){
                    WrBack.add(loadMemory);
                    loadMemory = null;
                }
                if(buf9 != null){
                    WrBack.add(buf9);
                    buf9 = null;
                }
                if(buf8 != null){
                    buf11 = buf8;
                    buf8 = null;
                }
                if(buf7 != null){
                    WrBack.add(buf7);
                    buf7 = null;
                }
                if(divisionInstr != null){
                    divCount++;
                    if(divCount == 4){
                        buf7 = divisionInstr;
                        divisionInstr = null;
                    }
                }
                if(buf6 != null){
                    //System.out.println("Load/Store:: " + disassembleInstruction(buf6) + "\t" + buf6);
                    if(buf6.substring(0, 6).equals("000100")){
                        //System.out.println("Store:: " + disassembleInstruction(buf6) + "\t" + buf6);
                        WrBack.add(buf6);
                    }
                    else if (buf6.substring(0, 6).equals("000101")){
                        //System.out.println("Load:: " + disassembleInstruction(buf6) + "\t" + buf6);
                        buf10 = buf6;
                    }
                    buf6 = null;
                }
                if(!buf5.isEmpty()){
                    if(buf5.size() == 2)
                        bufFull5 = true;
                    buf9 = buf5.get(0);
                    buf5.remove(0);
                } */

//                if(!buf1.isEmpty()){
//                    Iterator<String> it = buf1.iterator();
//                    while (it.hasNext()) {
//                        String bufInstr = it.next();
//                        String instrType = bufInstr.substring(0, 6);
//                        if(instrType.equals("000100") || instrType.equals("000101")){
//                            // ALU2 Unit
//                            if(!buf2Full && buf2.size() < 2 && executeInstruction(bufInstr, false)){
//                                buf2.add(bufInstr);
//                                it.remove();
//                            }
//                        }
                        /*else if(instrType.equals("011001")){
                            // Div Instruction
                            divisionIssued = true;
                            if(!bufFull3 && buf3.size() < 2 && lowUsed == false && executeInstruction(bufInstr, false)){
                                lowUsed = true;
                                highUsed = true;
                                buf3.add(bufInstr);
                                it.remove();
                            }
                        }*/
                        /*else if(instrType.equals("011000")){
                            // Mult Instruction
                            mulplicationIssued = true;
                            if(!bufFull4 && buf4.size() < 2 && lowUsed == false && executeInstruction(bufInstr, false)){
                                lowUsed = true;
                                buf4.add(bufInstr);
                                it.remove();
                            }//
                        }
                        else {
                            // ALU1 Unit
                            if(!bufFull5 && buf5.size() < 2 && executeInstruction(bufInstr, false)){
                                buf5.add(bufInstr);
                                it.remove();
                            }
                        }*/
//                    }
//                }
                if(instructionFetch != null && instructionFetch.substring(0, 6).equals("000000")){
                    //System.out.println("JUMP" + posCntr);
                    instructionFetch = null;
                    isExecuted = false;
                    itswaiting = false;
                }
                if(instructionFetch != null){
					/*String bits = instrFetch.substring(6);
					String opCode = instrFetch.substring(3, 6);
					int src1 = Integer.parseInt(bits.substring(0, 5), 2);
					int src2 = Integer.parseInt(bits.substring(5, 10), 2);
					int offset = Integer.parseInt(bits.substring(10), 2) << 2;
					if ((opCode.equals("011") && !regRead.contains(src1))
							|| (!opCode.equals("011") && !regRead.contains(src1) && !regRead.contains(src2)){

					}*/
//                    if (ReadReg.size() == 0 && WriteReg.size() == 0) {
                    boolean hazzard = false;
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
                    //System.out.println("Category Jump:: " + instrFetch + "\tposCntr: " + posCntr + "\tnextPosCntr: " + nextPosCntr);
                    if(posCntr == -1){
                        posCntr = nextPositionCnt;
                        itswaiting = true;
                    }
                    else {
                        if(isExecuted){
                            itswaiting = false;
                            instructionFetch = null;
                            if (posCntr == nextPositionCnt)
                                nextPositionCnt += 4;
                            else
                                nextPositionCnt = posCntr;
                        }
                        else {
                            posCntr = nextPositionCnt;
                            isExecuted = true;
                        }
                    }

                }
                ifCount = 0;
                while(ifCount < 4 && buf1.size() < 8 && itswaiting == false){
                    posCntr = nextPositionCnt;
                    nextInstr = hm.get(posCntr);
                    //System.out.println(posCntr + "\t\t" + nextInstr + "\t\t" + disassembleInstruction(nextInstr));
                    if(nextInstr.compareTo("00011000000000000000000000000000") == 0){
                        instructionFetch = nextInstr;
                        itswaiting = true;
                        isExecuted = true;
                        nextPositionCnt = dataCounter;
                        break;
                    }
                    if(nextInstr.substring(0, 4).equals("0000")){
                        instructionFetch = nextInstr;
                        itswaiting = true;
                        if(instructionFetch.substring(0, 6).equals("000000")){
                            //System.out.println("JUMP");
                            nextPositionCnt = processCategoryJumps(posCntr, instructionFetch, new HashSet<>());
                            isExecuted = true;
                        }
                        else
                            isExecuted = false;
                        break;
                    }
                    buf1.add(nextInstr);
                    ifCount++;
                    if (posCntr == nextPositionCnt)
                        nextPositionCnt += 4;
                }
                if(!WrBack.isEmpty()){
                    Iterator<String> wb = WrBack.iterator();
                    while(wb.hasNext()){
                        String s = wb.next();
                        executeInstruction(s, true);
                        wb.remove();
                    }
                }
                //****** End Execution Cycle ******
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


    //********** Execute Instructions **********
    private static boolean executeInstruction(String nextInstr, boolean execute) {
        // TODO Auto-generated method stub
        String instrCategory = nextInstr.substring(0, 3);
        switch (instrCategory) {
            case "000":		//*** Category 1
                return processCat1(nextInstr, execute);
            case "001":		//*** Category 2
                return processCat2(nextInstr, execute);
            case "010":		//*** Category 3
                return processCat3(nextInstr, execute);
            /*case "011":		//**** Category 4
                return processCategoryFour(nextInstr, execute);
            case "100":		//*** Category 5
                return processCategoryFive(nextInstr, execute);*/
            default:		//*** Input Error
                //*** should not execute (errors not handled) ***
                System.out.println(nextInstr + " : Invalid Instruction");
                return false;	//Exit program
        }
    }


    //********** Category Jumps **********
    private static int processCategoryJumps(int posCntr, String mipsInstr, HashSet<Integer> buf1Write) {
        // Format of Instructions in Category-1
        String opCode = mipsInstr.substring(3, 6);
        String bits = mipsInstr.substring(6);
        //int data = 0;
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
                if(WriteReg.contains(src1) || WriteReg.contains(src2) || buf1Write.contains(src1) || buf1Write.contains(src2))
                    return -1;
                if (ArrayRegister[src1] == ArrayRegister[src2]){
                    posCntr = posCntr + 4 + offset;
                }
                break;
            case "010":
                // BNE
                if(WriteReg.contains(src1) || WriteReg.contains(src2) || buf1Write.contains(src1) || buf1Write.contains(src2))
                    return -1;
                if (ArrayRegister[src1] != ArrayRegister[src2]){
                    posCntr = posCntr + 4 + offset;
                }
                break;
            case "011":
                // BGTZ
                if(WriteReg.contains(src1) || buf1Write.contains(src1))
                    return -1;
                if (ArrayRegister[src1] > 0){
                    posCntr = posCntr + 4 + offset;
                }
                break;
        }
        return posCntr;
    }


    //********** Category One **********
    private static boolean processCat1(String mipsInstr, boolean execute) {
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
                    //System.out.println("Store -- DataMap: " + ((offset >> 2) + ArrayRegister[src1]) + "\toffset " + src1 + "\t" + ArrayRegister[src1] + "\tReg " + src2 + "\tRegVal " + ArrayRegister[src2]);
                    dataMap.put((offset >> 2) + ArrayRegister[src1], ArrayRegister[src2]);
                    ReadReg.remove(src1);
                    ReadReg.remove(src2);
                }
                else {
                    OrderReg.add(src1);
                    OrderReg.add(src2);
                    if (WriteReg.contains(src1) || WriteReg.contains(src2))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(src2);
                }
                break;
            case "101":
                // LW
                if (execute){
                    ArrayRegister[src2] = dataMap.get((offset >> 2)+ ArrayRegister[src1]);
                    //System.out.println("Load:: "+ mipsInstr + "\t" + src2 + "\t" + ArrayRegister[src2] + "\t" + ((offset >> 2) + ArrayRegister[src1]));
                    ReadReg.remove(src1);
                    WriteReg.remove(src2);
                    int a = 1+2;
                }
                else {
                    if (ReadReg.contains(src2) || WriteReg.contains(src2))
                        return false;
                    ReadReg.add(src1);
                    WriteReg.add(src2);
                }
                break;
        }
        return true;
    }


    //********** Category Two **********
    private static boolean processCat2(String mipsInstr, boolean execute) {
        // Format of Instructions in Category-2
        String opCode = mipsInstr.substring(3, 6);
        int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
        int src1 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
        int src2 = Integer.parseInt(mipsInstr.substring(16, 21), 2);
        switch (opCode) {
            case "000":
                //ADD
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] + ArrayRegister[src2];
                    ReadReg.remove(src1);
                    ReadReg.remove(src2);
                    WriteReg.remove(dest);
                }
                else {
                    if (WriteReg.contains(src1) || WriteReg.contains(src2) ||
                            WriteReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(src2);
                    WriteReg.add(dest);
                }
                break;
            case "001":
                //SUB
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] - ArrayRegister[src2];
                    ReadReg.remove(src1);
                    ReadReg.remove(src2);
                    WriteReg.remove(dest);
                }
                else {
                    if (WriteReg.contains(src1) || WriteReg.contains(src2) ||
                            WriteReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(src2);
                    WriteReg.add(dest);
                }
                break;
            case "010":
                //AND
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] & ArrayRegister[src2];
                    ReadReg.remove(src1);
                    ReadReg.remove(src2);
                    WriteReg.remove(dest);
                }
                else {
                    if (WriteReg.contains(src1) || WriteReg.contains(src2) ||
                            WriteReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(src2);
                    WriteReg.add(dest);
                }
                break;
            case "011":
                //OR
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] | ArrayRegister[src2];
                    ReadReg.remove(src1);
                    ReadReg.remove(src2);
                    WriteReg.remove(dest);
                }
                else {
                    if (WriteReg.contains(src1) || WriteReg.contains(src2) ||
                            WriteReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(src2);
                    WriteReg.add(dest);
                }
                break;
            case "100":
                //SRL
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] >>> 2;
                    ReadReg.remove(src1);
                    WriteReg.remove(dest);
                }
                else {
                    if (WriteReg.contains(src1) ||
                            WriteReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    WriteReg.add(dest);
                }
                break;
            case "101":
                //SRA
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] >> 2;
                    ReadReg.remove(src1);
                    WriteReg.remove(dest);
                }
                else {
                    if (WriteReg.contains(src1) ||
                            WriteReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    WriteReg.add(dest);
                }
                break;
            case "110":
                //MUL
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] * ArrayRegister[src2];
                    ReadReg.remove(src1);
                    ReadReg.remove(src2);
                    WriteReg.remove(dest);
                }
                else {
                    if (WriteReg.contains(src1) || WriteReg.contains(src2) ||
                            WriteReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(src2);
                    WriteReg.add(dest);
                }

        }
        return true;
    }


    //********** Category Three **********
    private static boolean processCat3(String mipsInstr, boolean execute) {
        // Format of Instructions in Category-3
        String opCode = mipsInstr.substring(3, 6);
        int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
        int src1 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
        int data = Integer.parseInt(mipsInstr.substring(16), 2);
        switch (opCode) {
            case "000":
                //ADDI
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] + data;
                    ReadReg.remove(src1);
                    ReadReg.remove(dest);
                }
                else {
                    if(OrderReg.contains(src1) || OrderReg.contains(dest))
                        return false;
                    if (ReadReg.contains(src1) || ReadReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(dest);
                }
                break;
            case "001":
                //ANDI
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] & data;
                    ReadReg.remove(src1);
                    ReadReg.remove(dest);
                }
                else {
                    if (ReadReg.contains(src1) ||
                            ReadReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(dest);
                }
                break;
            case "010":
                //ORI
                if (execute){
                    ArrayRegister[dest] = ArrayRegister[src1] | data;
                    ReadReg.remove(src1);
                    ReadReg.remove(dest);
                }
                else {
                    if (ReadReg.contains(src1) ||
                            ReadReg.contains(dest) || ReadReg.contains(dest))
                        return false;
                    ReadReg.add(src1);
                    ReadReg.add(dest);
                }
                break;
        }
        return true;
    }


//********** Print the simulation.txt output file ***********


    //********** Print Simulation State **********
    private static void printSimulationState(BufferedWriter bw) {
        cyls++;
        //if(cycle > 100)
        //	System.exit(0);
        //System.out.println("Cycle:  " + cycle);
        try {
            bw.write("--------------------");
            bw.write("\nCycle " + cyls + ":");
            bw.write("\n\nIF:");
            if(!itswaiting){
                bw.write("\n\tWaiting:");
                bw.write("\n\tExecuted:");
            }
            else {
                if (isExecuted){
                    bw.write("\n\tWaiting:");
                }
                else {
                    bw.write("\n\tExecuted:");

                }
            }
            //**********
            bw.write("\nBuf1:");
            int loop = 0;
            for(loop=0; loop<buf1.size(); loop++){
            }
            while(loop<8){
                bw.write("\n\tEntry " + loop++ + ":");
            }
            //**********
            bw.write("\nBuf2:");
            for(loop=0; loop<buf2.size(); loop++){
            }
            while(loop<2){
                bw.write("\n\tEntry " + loop++ + ":");
            }
            //**********
            bw.write("\nBuf3:");
            int loop2 = 0;
            for(loop=0; loop<buf3.size(); loop++){
            }
            while(loop<2){
                bw.write("\n\tEntry " + loop++ + ":");
            }
            //**********
            bw.write("\nBuf4:");
            for(loop=0; loop<buf4.size(); loop++){
            }
            while(loop<2){
                bw.write("\n\tEntry " + loop++ + ":");
            }
            bw.write("\nBuf5:");
            if(buf5 != null)
                bw.write("\nBuf6:");
            if(buf6 != null){
                bw.write(" [" + printBufferSix(buf6) + "]");
            }
            bw.write("\nBuf7:");
            if(buf7 != null)
                bw.write("\nBuf8:");
            if(buf8 != null){
                String bits = buf8.substring(6);
                int src1 = Integer.parseInt(bits.substring(0, 5), 2);
                int src2 = Integer.parseInt(bits.substring(5, 10), 2);
                int offset = Integer.parseInt(bits.substring(10), 2) << 2;
                ArrayRegister[src2] = dataMap.get((offset >> 2)+ ArrayRegister[src1]);
                bw.write(" [" + dataMap.get((offset >> 2)+ ArrayRegister[src1])+ ", R" + src2 + "]");
            }
            bw.write("\nBuf9:");
            if(buf9 != null)
                bw.write("\nBuf10:");
            if(buf10 != null){
                int src1 = Integer.parseInt(buf10.substring(6, 11), 2);
                int src2 = Integer.parseInt(buf10.substring(11, 16), 2);
                bw.write(" [" + (ArrayRegister[src1] * ArrayRegister[src2]) + "]");
            }
            bw.write("\n\nRegisters");
            for(int i=0; i<32; i++){
                if (i%8 == 0){
                    bw.write("\n");
                    bw.write(String.format("R%02d:", i));
                }
                bw.write("\t" + ArrayRegister[i]);
            }
            /*bw.write("\nHI:\t" + High);
            bw.write("\nLO:\t" + Low);*/
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


    //********** Print Buffer 6 State **********
    private static String printBufferSix(String buf6) {
        String result = "";
        if(buf6.startsWith("001")){
            String opCode = buf6.substring(3, 6);
            int dest = Integer.parseInt(buf6.substring(6, 11), 2);
            int src1 = Integer.parseInt(buf6.substring(11, 16), 2);
            int src2 = Integer.parseInt(buf6.substring(16, 21), 2);
            switch (opCode) {
                case "000":
                    //ADD
                    result += (ArrayRegister[src1] + ArrayRegister[src2]);
                    break;
                case "001":
                    //SUB
                    result += (ArrayRegister[src1] - ArrayRegister[src2]);
                    break;
                case "010":
                    //AND
                    result += (ArrayRegister[src1] & ArrayRegister[src2]);
                    break;
                case "011":
                    //OR
                    result += (ArrayRegister[src1] | ArrayRegister[src2]);
                    break;
                case "100":
                    //SRL
                    result += (ArrayRegister[src1] >>> 2);
                    break;
                case "101":
                    //SRA
                    result += (ArrayRegister[src1] >> 2);
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
                    result += (ArrayRegister[src1] + data);
                    break;
                case "001":
                    //ANDI
                    result += (ArrayRegister[src1] & data);
                    break;
                case "010":
                    //ORI
                    result += (ArrayRegister[src1] | data);
                    break;
            }
            result += ", R" + dest;
        }
        return result;
    }
}
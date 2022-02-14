import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * SUPER STARTREK - MAY 16,1978
 * ****        **** STAR TREK ****        ****
 * **** SIMULATION OF A MISSION OF THE STARSHIP ENTERPRISE,
 * **** AS SEEN ON THE STAR TREK TV SHOW.
 * **** ORIGIONAL PROGRAM BY MIKE MAYFIELD, MODIFIED VERSION
 * **** PUBLISHED IN DEC'S "101 BASIC GAMES", BY DAVE AHL.
 * **** MODIFICATIONS TO THE LATTER (PLUS DEBUGGING) BY BOB
 * *** LEEDOM - APRIL & DECEMBER 1974,
 * *** WITH A LITTLE HELP FROM HIS FRIENDS . . .
 * Ported to Java in January 2022 by
 * Taciano Dreckmann Perez (taciano.perez@gmail.com)
 */
public class SuperStarTrekGame {

    static final Random random = new Random();

    static final String Z$ = "                         ";
    static final String A1$="NAVSRSLRSPHATORSHEDAMCOMXXX";

    // state matrices
    final int G[][] = new int[9][9];    // 8x8 galaxy map
    final int C[][] = new int[10][3];   // 9x2 vectors in cardinal directions
    final int K[][] = new int[4][4];    // 3x3 position of klingons
    final int N[] = new int[4];         // 3
    final int Z[][] = new int[9][9];    // 8x8 charted galaxy map
    final double D[] = new double[9];   // 8  device damage stats

    String X$ = "";
    String X0$ = " IS ";
    String Q$=Z$+Z$+Z$+Z$+Z$+Z$+Z$+LEFT$(Z$,17);       // current quadrant map

    // state integers
    double T = INT(RND1() * 20 + 20); // current stardate
    int E = 3000;   // current energy
    int D0 = 0;     // ship docked boolean
    int P = 10;     // number of photon torpedoes
    int S = 0;      // shields
    int T9 = 25 + INT(RND1()*10);    // numOfDays
    int B9 = 0;   // totalNumOfBases ?
    int K7;     // ? remaining number of Klingons at end game?
    int K9 = 0;   // totalNumOfKlingons?

    int Q1;         // current quadrant X
    int Q2;         // current quadrant Y
    int S1;         // current sector X
    int S2;         // current sector Y

    int K3 = 0; // numOfKlingons
    int B3 = 0;   // numOfStarbases
    int B4 = 0;   // X coordinate of starbase
    int B5 = 0; // Y coord of starbase
    int S3 = 0;   // numOfStars
    double D4;  // related to damage

    boolean restart = false;

    // constants
    final double T0 = T;   // initial stardate
    final int E0 = E;      // initial energy
    final int P0=P;        // initial number of photon torpedoes
    final int S9=200;

    public static void main(String[] args) {
        final SuperStarTrekGame game = new SuperStarTrekGame();
        printBanner();
        while (true) {
            game.setup();
            game.enterNewQuadrant();
            game.restart = false;
            game.commandLoop();
        }
    }

    static void printBanner() {     // 220
        IntStream.range(1,10).forEach(i->{PRINT("");});
        PRINT(
                """
                                                    ,------*------,
                                    ,-------------   '---  ------'
                                     '-------- --'      / /
                                         ,---' '-------/ /--,
                                          '----------------'
                                          
                                    THE USS ENTERPRISE --- NCC-1701"
                        
                """
        );
    }

    //    DEF FND(D)=SQR((K(I,1)-S1)^2+(K(I,2)-S2)^2)
    double fnd(int I) { // 470
        return Math.sqrt((K[I][1]-S1)^2+(K[I][2]-S2)^2);
    }

    // DEF FNR(R)=INT(RND(R)*7.98+1.01)
    int fnr(int R) {    // 475
        // FIXME: can the random number generator be zero or negative here?
        //return INT(RND1()*7.98+1.01);
        // Generate a random integer from 1 to 8 inclusive.
        return INT(RND1()*7+1);
    }

    void setup() {
        this.initEnterprisePosition();
        this.setupWhatExistsInTheGalaxy();
    }

    void initEnterprisePosition() {     // 480
        Q1 = fnr(1);
        Q2 = fnr(1);
        S1 = fnr(1);
        S2 = fnr(1);
        IntStream.range(1,9).forEach( i-> { C[i][1] = 0; C[i][2] = 0; });
        C[3][1]=-1;C[2][1]=-1;C[4][1]=-1;C[4][2]=-1;C[5][2]=-1;C[6][2]=-1;
        C[1][2]=1;C[2][2]=1;C[6][1]=1;C[7][1]=1;C[8][1]=1;C[8][2]=1;C[9][2]=1;
        IntStream.range(1,8).forEach(i-> D[i] = 0);
    }

    void setupWhatExistsInTheGalaxy() {     // 810
        // K3= # KLINGONS  B3= # STARBASES  S3= # STARS
        IntStream.range(1,8).forEach(i-> {
            IntStream.range(1,8).forEach(j-> {
                K3 = 0;
                Z[i][j]=0;
                float R1=RND1();
                if (R1>.98) {
                    K3=3;
                    K9=+3;
                } else if (R1>.95) {
                    K3=2;
                    K9=+2;
                } else if (R1>.80) {
                    K3=1;
                    K9=+1;
                }
                B3 = 0;
                if (RND1() > .96) {
                    B3=1;
                    B9=+1;
                }
                G[i][j] = K3 * 100 + B3 * 10 + fnr(1);
            });
        });
        if (K9 > T9) T9 = K9 + 1;
        if (B9 == 0) {
            if (G[Q1][Q2] < 200) {
                G[Q1][Q2] = G[Q1][Q2] + 120;
                K9 =+ 1;
            } 
            B9 = 1;
            G[Q1][Q2] =+ 10;
            Q1=fnr(1);
            Q2=fnr(1);
        }
        K7=K9;
        if (B9 != 1) {
            X$="S";
            X0$=" ARE ";
        }
        PRINT("YOUR ORDERS ARE AS FOLLOWS:\n" +
                "     DESTROY THE " + K9 +" KLINGON WARSHIPS WHICH HAVE INVADED\n" +
                "   THE GALAXY BEFORE THEY CAN ATTACK FEDERATION HEADQUARTERS\n" +
                "   ON STARDATE " + T0 + T9 +"  THIS GIVES YOU "+ T9 + " DAYS.  THERE "+ X0$ + "\n" +
                "  "+B9+" STARBASE "+X$+" IN THE GALAXY FOR RESUPPLYING YOUR SHIP");
        float I = RND1();
    }

    void enterNewQuadrant() {   // 1320
        // ANY TIME NEW QUADRANT ENTERED
        int Z4=Q1;
        int Z5=Q2;
        K3=0;
        B3=0;
        S3=0;   // numOfStars
        int G5=0;
        D4 = .5 * RND1();
        Z[Q1][Q2] = G[Q1][Q2];
        if (!(Q1<1 || Q1>8 || Q2<1 || Q2>8)) {
          final String G2$ = getQuadrantName(G5, Z4, Z5);
          if (T0 == T) {
              PRINT( "YOUR MISSION BEGINS WITH YOUR STARSHIP LOCATED\n" +
                                "IN THE GALACTIC QUADRANT, '"+G2$+"'.");
          } else {
              PRINT( "NOW ENTERING "+G2$+" QUADRANT . . .");
          }
          PRINT("");
          K3 = (int)Math.round(G[Q1][Q2]*.01);
          B3 = (int)Math.round(G[Q1][Q2]*.1)-10*K3;
          S3 = G[Q1][Q2]-100*K3-10*B3;
          if (K3 != 0) {
              PRINT("COMBAT AREA      CONDITION RED");
              if (S <= 200) {
                  PRINT("   SHIELDS DANGEROUSLY LOW");
              }
          }
          IntStream.range(1,3).forEach(i-> {
            K[i][1]=0;
            K[i][2]=0;
          });
        }
        IntStream.range(1,3).forEach(i-> {
            K[i][3]=0;
        });
        // POSITION ENTERPRISE IN QUADRANT, THEN PLACE "K3" KLINGONS, &
        // "B3" STARBASES, & "S3" STARS ELSEWHERE.
        String A$="<*>";
        int Z1=S1;
        int Z2=S2;
        Q$ = insertMarker(A$, Z1, Z2, Q$);
        // position Klingons
        if (K3 >= 1) {
            for (int i=1; i<=K3; i++) {
                final int[] emptyCoordinate = findEmptyPlaceInQuadrant(Q$);
                A$="+K+";
                Z1=emptyCoordinate[0];
                Z2=emptyCoordinate[1];
                Q$ = insertMarker(A$, Z1, Z2, Q$);
                K[i][1] = emptyCoordinate[0];
                K[i][2] = emptyCoordinate[1];
                K[i][3] = (int)Math.round(S9*(0.5+RND1()));
            }
        }
        // position Bases
        if (B3 >= 1) {
            final int[] emptyCoordinate = findEmptyPlaceInQuadrant(Q$);
            A$ = ">!<";
            Z1 = emptyCoordinate[0];
            B4 = emptyCoordinate[0];
            Z2 = emptyCoordinate[1];
            B5 = emptyCoordinate[1];
            Q$ = insertMarker(A$, Z1, Z2, Q$);
        }
        // position stars
        for (int i=1; i<=S3; i++) {
            final int[] emptyCoordinate = findEmptyPlaceInQuadrant(Q$);
            A$=" * ";
            Z1=emptyCoordinate[0];
            Z2=emptyCoordinate[1];
            Q$ = insertMarker(A$, Z1, Z2, Q$);
        }
        shortRangeSensorScan(); // 1980
    }

    void commandLoop() {
        while (!this.restart) {
            checkShipEnergy();    // 1990
            String A$ = "";
            while ("".equals(A$)) A$ = INPUT("COMMAND");
            boolean foundCommand = false;
            for (int i = 1; i <= 9; i++) {
                //DEBUG(LEFT$(A$, 3) + " COMPARE TO " + MID$(A1$, 3 * i - 2, 3));
                if (LEFT$(A$, 3).equals(MID$(A1$, 3 * i - 2, 3))) {
                    //DEBUG(LEFT$(A$, 3) + " IS EQUAL TO " + MID$(A1$, 3 * i - 2, 3));
                    switch (i) {
                        case 1:
                            navigation();
                            foundCommand = true;
                            break;
                        case 2:
                            shortRangeSensorScan();
                            foundCommand = true;
                            break;
                        case 3:
                            longRangeSensorScan();
                            foundCommand = true;
                            break;
                        case 4:
                            firePhasers();
                            foundCommand = true;
                            break;
                        case 5:
                            firePhotonTorpedo();
                            foundCommand = true;
                            break;
                        case 6:
                            shieldControl();
                            foundCommand = true;
                            break;
                        case 7:
                            damageControl();
                            foundCommand = true;
                            break;
                        case 8:
                            libraryComputer();
                            foundCommand = true;
                            break;
                        case 9:
                            endGameFail(false);
                            foundCommand = true;
                            break;
                        default:
                            printCommandOptions();
                            foundCommand = true;
                    }
                }
            }
            if (!foundCommand) printCommandOptions();
        }
    }

    void checkShipEnergy() {
        int totalEnergy = (S+E);
        if (totalEnergy < 10 && (E <= 10 || D[7] != 0 )) {
            PRINT("\n** FATAL ERROR **   YOU'VE JUST STRANDED YOUR SHIP IN ");
            PRINT( "SPACE");
            PRINT( "YOU HAVE INSUFFICIENT MANEUVERING ENERGY,");
            PRINT( " AND SHIELD CONTROL");
            PRINT( "IS PRESENTLY INCAPABLE OF CROSS");
            PRINT( "-CIRCUITING TO ENGINE ROOM!!");
            endGameFail(false);
        }
    }

    void printCommandOptions() {
        PRINT( "ENTER ONE OF THE FOLLOWING:");
        PRINT( "  NAV  (TO SET COURSE)");
        PRINT( "  SRS  (FOR SHORT RANGE SENSOR SCAN)");
        PRINT( "  LRS  (FOR LONG RANGE SENSOR SCAN)");
        PRINT( "  PHA  (TO FIRE PHASERS)");
        PRINT( "  TOR  (TO FIRE PHOTON TORPEDOES)");
        PRINT( "  SHE  (TO RAISE OR LOWER SHIELDS)");
        PRINT( "  DAM  (FOR DAMAGE CONTROL REPORTS)");
        PRINT( "  COM  (TO CALL ON LIBRARY-COMPUTER)");
        PRINT( "  XXX  (TO RESIGN YOUR COMMAND)\n");
    }

    void navigation() {  // 2290
        float C1 = INT(INPUTNUM("COURSE (0-9)"));
        if (C1 == 9) C1 = 1;
        if (C1 < 1 || C1 >= 9) {
            PRINT("   LT. SULU REPORTS, 'INCORRECT COURSE DATA, SIR!'");
            return;
        }
        X$="8";
        if (D[1]<0) X$="0.2";
        PRINT("WARP FACTOR (0-"+X$+")");
        float W1 = INPUTNUM("");
        if (D[1]<0 && W1>.2) {
            // 2470
            PRINT("WARP ENGINES ARE DAMAGED.  MAXIMUM SPEED = WARP 0.2");
            return;
        }
        if (W1 == 0) return;
        if (W1 > 0 && W1 <= 8) {
            // 2490
            int N=INT(W1*8);
            if (E-N>=0) {
                klingonsMoveAndFire(C1, W1, N);
                repairDamagedDevices(C1, W1, N);
                beginMovingStarship(C1, W1, N);
            } else {
                PRINT("ENGINEERING REPORTS   'INSUFFICIENT ENERGY AVAILABLE");
                PRINT("                       FOR MANEUVERING AT WARP"+W1+"!'");
                if (S<N-E || D[7]<0) return;
                PRINT("DEFLECTOR CONTROL ROOM ACKNOWLEDGES"+S+"UNITS OF ENERGY");
                PRINT("                         PRESENTLY DEPLOYED TO SHIELDS.");
                return;
            }
        } else {
            PRINT("   CHIEF ENGINEER SCOTT REPORTS 'THE ENGINES WON'T TAKE");
            PRINT(" WARP "+W1+"!'");
            return;
        }
    }

    void klingonsMoveAndFire(final float C1, final float W1, final int N) { // 2590
        // KLINGONS MOVE/FIRE ON MOVING STARSHIP . . .
        for (int i=1; i <= K3; i++) {
            if (K[i][3] == 0) continue;
            String A$ = "   ";
            int Z1 = K[i][1];
            int Z2 = K[i][2];
            Q$ = insertMarker(A$, Z1, Z2, Q$);
            final int[] coords = findEmptyPlaceInQuadrant(Q$);
            Z1 = K[i][1] = coords[0];
            Z2 = K[i][2] = coords[1];
            A$="+K+";
            Q$ = insertMarker(A$, Z1, Z2, Q$);
        }
        klingonsShoot();
    }

    void repairDamagedDevices(final float C1, final float W1, final int N) {
        // repair damaged devices and print damage report
        int D1=0;
        float D6=W1;
        if (W1>=1) D6=1;
        for (int i=1; i <= 8; i++) {
            if (D[i] >= 0) continue;
            D[i] += D6;
            if ((D[i] > -.1) && (D[i] < 0)) {
                D[i] = -.1;
                break;
            }
            if (D[i] < 0) continue;
            if (D1 != 1) {
                D1 = 1;
                PRINT("DAMAGE CONTROL REPORT:  ");
            }
            PRINT(TAB(8) + printDeviceName(i) + " REPAIR COMPLETED.");
        }
        if (RND1()>.2) beginMovingStarship(C1, W1, N);  // 80% chance no damage nor repair
        int R1 = fnr(1);    // random device
        if (RND1()>=.6) {   // 59% chance of repair of random device
            D[R1]=D[R1]+RND1()*3+1;
            PRINT("DAMAGE CONTROL REPORT:  " + printDeviceName(R1) + " STATE OF REPAIR IMPROVED\n");
        } else {            // 41% chance of damage of random device
            D[R1]=D[R1]-(RND1()*5+1);   //
            PRINT("DAMAGE CONTROL REPORT:  " + printDeviceName(R1) + " DAMAGED");
        }
    }

    void beginMovingStarship(final float C1, final float W1, final int N) {    // 3070
//        3060 REM BEGIN MOVING STARSHIP
        String A$="   ";
        int Z1=INT(S1);
        int Z2=INT(S2);
        Q$ = insertMarker(A$, Z1, Z2, Q$);
        int ic1 = INT(C1);
        float X1=C[ic1][1]+(C[ic1+1][1]-C[ic1][1])*(C1-ic1);
        float X=S1;
        float Y=S2;
        float X2=C[ic1][2]+(C[ic1+1][2]-C[ic1][2])*(C1-ic1);
        int Q4=Q1;
        int Q5=Q2;
        for (int i=1; i <= N; i++) {
            S1+=X1;
            S2+=X2;
            if (S1<1 || S1>=9 || S2<1 || S2>=9) {
                // exceeded quadrant limits
                X=8*Q1+X+N*X1;
                Y=8*Q2+Y+N*X2;
                Q1=INT(X/8);
                Q2=INT(Y/8);
                S1=INT(X-Q1*8);
                S2=INT(Y-Q2*8);
                if (S1==0) {
                    Q1=Q1-1;
                    S1=8;
                }
                if (S2==0) {
                    Q2=Q2-1;
                    S2=8;
                }
                int X5=0;   // hit edge boolean
                if (Q1<1) {
                    X5=1;
                    Q1=1;
                    S1=1;
                }
                if (Q1>8) {
                    X5=1;
                    Q1=8;
                    S1=8;
                }
                if (Q2 < 1) {
                    X5 = 1;
                    Q2=8;
                    S2=8;
                }
                if (Q2>8) {
                    X5=1;
                    Q2=8;
                    S2=8;
                }
                if (X5!=0) {
                    PRINT("LT. UHURA REPORTS MESSAGE FROM STARFLEET COMMAND:");
                    PRINT("  'PERMISSION TO ATTEMPT CROSSING OF GALACTIC PERIMETER");
                    PRINT("  IS HEREBY *DENIED*.  SHUT DOWN YOUR ENGINES.'");
                    PRINT("CHIEF ENGINEER SCOTT REPORTS  'WARP ENGINES SHUT DOWN");
                    PRINT("  AT SECTOR "+S1+","+S2+" OF QUADRANT "+Q1+","+Q2+".'");
                    if (T > T0+T9) endGameFail(false);
                }
                if (8*Q1+Q2==8*Q4+Q5) {
                    break;
                }
                T=T+1;
                maneuverEnergySR(N);
                enterNewQuadrant();
                return;
            } else {
                int S8=INT(S1)*24+INT(S2)*3-26; // S8 = pos
                if (!("  ".equals(MID$(Q$,S8,2)))) {
                    S1 = INT(S1 - X1);
                    S2 = INT(S2 - X2);
                    PRINT("WARP ENGINES SHUT DOWN AT ");
                    PRINT("SECTOR " + S1 + "," + S2 + " DUE TO BAD NAVIGATION");
                    break;
                }
            }
        }
        S1=INT(S1);
        S2=INT(S2);
        // insert marker
        A$="<*>";   // 3370
        Z1=INT(S1);
        Z2=INT(S2);
        Q$ = insertMarker(A$, Z1, Z2, Q$);
        maneuverEnergySR(N);
        double T8=1;
        if (W1<1) T8=.1*INT(10*W1);
        T=T+T8;
        if (T>T0+T9) endGameFail(false);
        shortRangeSensorScan();
    }

    void maneuverEnergySR(final int N) {  // 3910
        E=E-N-10;
        if (E>=0) return;
        PRINT("SHIELD CONTROL SUPPLIES ENERGY TO COMPLETE THE MANEUVER.");
        S=S+E;
        E=0;
        if (S<=0) S=0;
        return;
    }

    void longRangeSensorScan() {    // 3390
        // LONG RANGE SENSOR SCAN CODE
        if (D[3]<0) {
            PRINT("LONG RANGE SENSORS ARE INOPERABLE");
            return;
        }
        PRINT("LONG RANGE SCAN FOR QUADRANT "+Q1+","+Q2);
        final String O1$="-------------------";
        PRINT(O1$);
        for (int i=Q1-1; i<=Q1+1; i++) {
            N[1]=-1;
            N[2]=-2;
            N[3]=-3;
            for (int j=Q2-1; j<= Q2+1; j++) {
                if (i>0 && i<9 && j>0 && j<9){
                    N[j-Q2+2]=G[i][j];
                    Z[i][j]=G[i][j];
                }
            }
            for (int l=1; l<= 3; l++) {
                PRINTS(": ");
                if (N[l]<0) {
                    PRINTS("*** ");
                    continue;
                }
                PRINTS(": " + RIGHT$(Integer.toString(N[l]+1000),3) + " ");
            }
            PRINT(": \n" + O1$);
        }
    }

    void firePhasers() {    // 4260
        // PHASER CONTROL CODE BEGINS HERE
        if (D[4]<0) {
            PRINT("PHASERS INOPERATIVE");
            return;
        }
        if (K3<=0) {
            printNoEnemyShipsMessage();
            return;
        }
        if (D[8]<0) PRINT("COMPUTER FAILURE HAMPERS ACCURACY");
        PRINT("PHASERS LOCKED ON TARGET;  ");
        int X;  // numOfUnitsToFire
        while (true) {
            PRINT("ENERGY AVAILABLE = " + E + " UNITS");
            X = INT(INPUTNUM("NUMBER OF UNITS TO FIRE"));
            if (X <= 0) return;
            if (E-X>=0) break;
        }
        E=E-X;
        if (D[7]<0) X=INT(X*RND1());
        int H1=INT(X/K3);
        for (int i=1; i<=3; i++) {
            if (K[i][3]<=0) break;
            int H=INT((H1/fnd(0))*(RND1()+2));
            if (H<=.15*K[i][3]) {
                PRINT("SENSORS SHOW NO DAMAGE TO ENEMY AT "+K[i][1]+","+K[i][2]);
                continue;
            }
            K[i][3]=K[i][3]-H;
            PRINT (H+" UNIT HIT ON KLINGON AT SECTOR "+K[i][1]+","+K[i][2]);
            if (K[i][3]<=0) {
                PRINT("*** KLINGON DESTROYED ***");
                K3=K3-1;
                K9=K9-1;
                int Z1=K[i][1];
                int Z2=K[i][1];
                String A$="   ";
                Q$ = insertMarker(A$, Z1, Z2, Q$);
                K[i][3]=0;
                G[Q1][Q2]=G[Q1][Q2]-100;
                Z[Q1][Q2]=G[Q1][Q2];
                if (K9<=0) endGameSuccess();
            } else {
                PRINT("   (SENSORS SHOW"+K[i][3]+"UNITS REMAINING)");
            }
        }
        klingonsShoot();
    }

    void printNoEnemyShipsMessage() {   // 4270
        PRINT("SCIENCE OFFICER SPOCK REPORTS  'SENSORS SHOW NO ENEMY SHIPS");
        PRINT("                                IN THIS QUADRANT'");
    }

    void firePhotonTorpedo() {  // 4700
        // PHOTON TORPEDO CODE BEGINS HERE
        if (P<=0) {
            PRINT("ALL PHOTON TORPEDOES EXPENDED");
            return;
        }
        if (D[5]<0) {
            PRINT("PHOTON TUBES ARE NOT OPERATIONAL");
        }
        float C1 = INPUTNUM("PHOTON TORPEDO COURSE (1-9)");
        if (C1==9) C1=1;
        if (C1<1 && C1>=9) {
            PRINT("ENSIGN CHEKOV REPORTS,  'INCORRECT COURSE DATA, SIR!'");
            return;
        }
        int iC1 = INT(C1);
        float X1=C[iC1][1]+(C[iC1+1][1]-C[iC1][1])*(C1-iC1);
        E=E-2;
        P=P-1;
        float X2=C[iC1][2]+(C[iC1+1][2]-C[iC1][2])*(C1-iC1);
        float X=S1;
        float Y=S2;
        PRINT("TORPEDO TRACK:");
        while (true) {
            X = X + X1;
            Y = Y + X2;
            int X3 = INT(X + .5); // FIXME: do we need this .5?
            int Y3 = INT(Y + .5);
            if (X3 < 1 || X3 > 8 || Y3 < 1 || Y3 > 8) {
                PRINT("TORPEDO MISSED"); // 5490
                klingonsShoot();
                return;
            }
            PRINT("               " + X3 + "," + Y3);
            String A$ = "   ";
            int Z1 = INT(X);
            int Z2 = INT(Y);
            int Z3 = compareMarker(Q$, A$, Z1, Z2);
            if (Z3 != 0) continue;
            A$="+K+";
            Z1 = INT(X);
            Z2 = INT(Y);
            Z3 = compareMarker(Q$, A$, Z1, Z2);
            if (Z3 != 0) {
                PRINT("*** KLINGON DESTROYED ***");
                K3=K3-1;
                K9=K9-1;
                if (K9<=0) endGameSuccess();
                for (int i=1; i<=3; i++) {
                    if (X3==K[i][1] && Y3==K[i][2]) break;
                }
                int i = 3;
                K[i][3] = 0;
            } else {
                A$=" * ";
                Z1 = INT(X);
                Z2 = INT(Y);
                Z3 = compareMarker(Q$, A$, Z1, Z2);
                if (Z3 != 0) {
                    PRINT("STAR AT "+X3+","+Y3+" ABSORBED TORPEDO ENERGY.");
                    klingonsShoot();
                    return;
                } else {
                    A$=">!<";
                    Z1 = INT(X);
                    Z2 = INT(Y);
                    Z3 = compareMarker(Q$, A$, Z1, Z2);
                    if (Z3!=0) {
                        PRINT("*** STARBASE DESTROYED ***");
                        B3=B3-1;
                        B9=B9-1;
                        if (B9 == 0 && K9 <= T-T0-T9) {
                            PRINT("THAT DOES IT, CAPTAIN!!  YOU ARE HEREBY RELIEVED OF COMMAND");
                            PRINT("AND SENTENCED TO 99 STARDATES AT HARD LABOR ON CYGNUS 12!!");
                            endGameFail(false);
                        } else {
                            PRINT("STARFLEET COMMAND REVIEWING YOUR RECORD TO CONSIDER");
                            PRINT("COURT MARTIAL!");
                            D0=0;   // docked = false
                        }
                    }
                }
            }
            Z1 = INT(X);
            Z2 = INT(Y);
            A$="   ";
            Q$ = insertMarker(A$, Z1, Z2, Q$);
            G[Q1][Q2]=K3*100+B3*10+S3;
            Z[Q1][Q2]=G[Q1][Q2];
            klingonsShoot();
        }
    }

    void shieldControl() {
        if (D[7]<0) {
            PRINT("SHIELD CONTROL INOPERABLE");
            return;
        }
        PRINT("ENERGY AVAILABLE = "+(E+S));
        int X = INT(INPUTNUM("NUMBER OF UNITS TO SHIELDS"));
        if(X<0 || S==X) {
            PRINT("<SHIELDS UNCHANGED>");
            return;
        }
        if (X > E+X) {
            PRINT("SHIELD CONTROL REPORTS  'THIS IS NOT THE FEDERATION TREASURY.'");
            PRINT("<SHIELDS UNCHANGED>");
            return;
        }
        E=E+S-X;
        S=X;
        PRINT("DEFLECTOR CONTROL ROOM REPORT:");
        PRINT("  'SHIELDS NOW AT "+INT(S)+" UNITS PER YOUR COMMAND.'");
    }

    void shortRangeSensorScan() { // 6430
        // SHORT RANGE SENSOR SCAN & STARTUP SUBROUTINE
        boolean docked = false;
        String C$; // ship condition (docked, red, yellow, green)
        for (int i = S1-1; i <= S1+1; i++) {
            for (int j = S2-1; j <= S2+1; j++) {
                if ((INT(i) >= 1) && (INT(i)<=8) && (INT(j)>=1) && (INT(j)<=8)) {
                    //6490 A$=">!<":Z1=I:Z2=J:GOSUB 8830:IF Z3=1 THEN 6580
                    final String A$ = ">!<";
                    final int Z1=i;
                    final int Z2=j;
                    if (compareMarker(Q$, A$, Z1, Z2) == 1) {
                        docked = true;
                    }
                }
            }
        }
        if (!docked) {
            D0 = 0;
            if (K3 > 0) {
                C$="*RED*";
            } else {
                C$="GREEN";
                if (E<E0*.1) {
                    C$="YELLOW";
                }
            }
        } else {
            D0 = 1;
            C$ = "DOCKED";
            E = E0;
            P = P0;
            PRINT("SHIELDS DROPPED FOR DOCKING PURPOSES");
            S = 0;
        }
        if (D[2] < 0) { // are short range sensors out?
            PRINT("\n*** SHORT RANGE SENSORS ARE OUT ***\n");
            return;
        }
        final String O1$ = "---------------------------------";
        PRINT(O1$);
        for (int i = 1; i <= 8; i++) {
            String sectorMapRow = "";
            for (int j = (i-1)*24+1; j <= (i-1)*24+22; j+=3) {
                sectorMapRow += " " + MID$(Q$,j,3);
            }
            switch (i) {
                case 1:
                    PRINT(sectorMapRow+"        STARDATE           "+INT(T*10)*.1);
                    break;
                case 2:
                    PRINT(sectorMapRow+"        CONDITION          "+C$);
                    break;
                case 3:
                    PRINT(sectorMapRow+"        QUADRANT           "+Q1+","+Q2);
                    break;
                case 4:
                    PRINT(sectorMapRow+"        SECTOR             "+S1+","+S2);
                    break;
                case 5:
                    PRINT(sectorMapRow+"        PHOTON TORPEDOES   "+INT(P));
                    break;
                case 6:
                    PRINT(sectorMapRow+"        TOTAL ENERGY       "+INT((E+S)));
                    break;
                case 7:
                    PRINT(sectorMapRow+"        SHIELDS            "+INT(S));
                    break;
                case 8:
                    PRINT(sectorMapRow+"        KLINGONS REMAINING "+INT(K9));
            };
        }
        PRINT(O1$); // 7260
    }

    void libraryComputer() {    // 7290
        // REM LIBRARY COMPUTER CODE
        if (D[8] < 0) {
            PRINT("COMPUTER DISABLED");
            return;
        }
        while (true) {
            final float fA = INPUTNUM("COMPUTER ACTIVE AND AWAITING COMMAND");
            if (fA < 0) return;
            PRINT("");
            int H8 = 1; // cumulative report (1) or galaxy map (0) ?
            int A = INT(fA) + 1;
            if (A >= 1 && A <= 6) {
                switch (A) {
                    case 1:
                        //GOTO 7540
                        cumulativeGalacticRecord(H8);
                        return;
                    case 2:
                        //GOTO 7900
                        statusReport();
                        return;
                    case 3:
                        //GOTO 8070
                        photonTorpedoData();
                        return;
                    case 4:
                        //GOTO 8500
                        starbaseNavData();
                        return;
                    case 5:
                        //GOTO 8150
                        directionDistanceCalculator();
                        return;
                    case 6:
                        //GOTO 7400
                        H8 = 0;
                        cumulativeGalacticRecord(H8);
                        return;
                }
            } else {
                // invalid command
                PRINT("FUNCTIONS AVAILABLE FROM LIBRARY-COMPUTER:");
                PRINT("   0 = CUMULATIVE GALACTIC RECORD");
                PRINT("   1 = STATUS REPORT");
                PRINT("   2 = PHOTON TORPEDO DATA");
                PRINT("   3 = STARBASE NAV DATA");
                PRINT("   4 = DIRECTION/DISTANCE CALCULATOR");
                PRINT("   5 = GALAXY 'REGION NAME' MAP");
                PRINT("");
            }
        }
    }

    void cumulativeGalacticRecord(final int H8) {   // 7540
        if (H8 == 1) {
            PRINT("");
            PRINT("        ");
            PRINT("COMPUTER RECORD OF GALAXY FOR QUADRANT " + Q1 + "," + Q2);
            PRINT("");
        } else {
            PRINT("                        THE GALAXY");
        }
        PRINT( "       1     2     3     4     5     6     7     8");
        final String O1$="     ----- ----- ----- ----- ----- ----- ----- -----";
        PRINT( O1$ );
        for (int i=1; i <= 8; i++) {
            PRINTS(i + "  ");
            if (H8==1) {
                int Z4=i;
                int Z5=1;
                String G2$ = getQuadrantName(0, Z4, Z5);
                int J0=INT(15-.5*LEN(G2$));
                PRINT( TAB(J0)+G2$);
                Z5=5;
                G2$ = getQuadrantName(0, Z4, Z5);
                J0=INT(39-.5*LEN(G2$));
                PRINT( TAB(J0)+G2$);
            } else {
                for (int j = 1; j <= 8; j++) {
                    PRINTS("   ");
                    if (Z[i][j] == 0) {
                        PRINTS("***");
                    } else {
                        PRINTS(RIGHT$(Integer.toString(Z[i][j] + 1000), 3));
                    }
                }
            }
            PRINT("");
            PRINT( O1$ );
        }
        PRINT("");
    }

    void statusReport() {   // 7900
        PRINT("   STATUS REPORT:");
        X$ = "";
        if (K9 > 1) X$ = "S";
        PRINT("KLINGON" + X$ + " LEFT: " + K9);
        PRINT("MISSION MUST BE COMPLETED IN " + .1 * INT((T0 + T9 - T) * 10) + "STARDATES");
        X$ = "S";
        if (B9 < 2) X$ = "";
        if (B9 >= 1) {
            PRINT("THE FEDERATION IS MAINTAINING " + B9 + " STARBASE" + X$ + " IN THE GALAXY");
        } else {
            PRINT("YOUR STUPIDITY HAS LEFT YOU ON YOUR OWN IN");
            PRINT("  THE GALAXY -- YOU HAVE NO STARBASES LEFT!");
        }
        damageControl();
    }

        void photonTorpedoData() {  // 8070
            // TORPEDO, BASE NAV, D/D CALCULATOR
            if (K3<=0) {
                printNoEnemyShipsMessage();
                return;
            }
            X$="";
            if (K3>1) X$="S";
            PRINT("FROM ENTERPRISE TO KLINGON BATTLE CRUISER"+X$);
            int H8=0;
            for (int i=1; i<=3; i++) {
                if (K[i][3] > 0) printDirection(S1, S2, K[i][1], K[i][2]);
            }
        }

        void directionDistanceCalculator() {    // 8150
            PRINT("DIRECTION/DISTANCE CALCULATOR:");
            PRINT("YOU ARE AT QUADRANT "+Q1+","+Q2+" SECTOR "+S1+","+S2);
            PRINTS("PLEASE ENTER ");
            int[] coords = INPUTCOORDS("  INITIAL COORDINATES (X,Y)");
            int C1 = coords[0];
            int A = coords[1];
            coords = INPUTCOORDS("  FINAL COORDINATES (X,Y)");
            int W1 = coords[0];
            int X = coords[1];
            printDirection(C1, A, W1, X);
        }

        void printDirection(int C1, int A, int W1, int X) { // from1, from2, to1, to2 // 8220
            X=X-A;  // delta 2
            A=C1-W1;    // delta 1
            if (X>0) {
                if (A < 0) {
                    C1 = 7;
                } else {
                    C1 = 1;
                    int tempA = A;
                    A = X;
                    X = tempA;
                }
            } else {
                if (A > 0) {
                    C1 = 3;
                } else {
                    C1 = 5;
                    int tempA = A;
                    A = X;
                    X = tempA;
                }
            }

            A = Math.abs(A);
            X = Math.abs(X);

            if (A > 0 || X > 0) {
                if (A >= X) {
                    PRINT("DIRECTION = " + (C1 + X / A));
                } else {
                    PRINT("DIRECTION = " + (C1 + 2 - X / A));
                }
            }
            PRINT("DISTANCE = "+round(Math.sqrt(X^2+A^2), 6));
        }

        void starbaseNavData() {    // 8500
            if (B3!=0) {
                PRINT("FROM ENTERPRISE TO STARBASE:");
                printDirection(S1, S2, B4, B5);
            } else {
                PRINT("MR. SPOCK REPORTS,  'SENSORS SHOW NO STARBASES IN THIS");
                PRINT(" QUADRANT.'");
            }
        }

    /**
     * Finds random empty coordinates in a quadrant.
     * @param Q$
     * @return an array with a pair of coordinates R1, R2
     */
    int[] findEmptyPlaceInQuadrant(String Q$) {   // 8590
        // FIND EMPTY PLACE IN QUADRANT (FOR THINGS)
        final int R1=fnr(1);
        final int R2=fnr(1);
        final String A$="   ";
        int Z1 = R1;
        int Z2 = R2;
        if (compareMarker(Q$, A$, Z1, Z2) == 0) {
            return findEmptyPlaceInQuadrant(Q$);
        }
        return new int[] {Z1, Z2};
    }


    String insertMarker(final String A$, final int Z1, final int Z2, String Q$) {   // 8670
        //S8=INT(Z2-.5)*3+INT(Z1-.5)*24+1
        int S8 = INT(Z2)*3 + INT(Z1)*24+1;  // S8 = pos
        if (A$.length() != 3) {
            System.err.println("ERROR");
            System.exit(-1);
        }
        if (S8 == 1) {
            return A$+RIGHT$(Q$,189);
        }
        if (S8 == 190) {
            return LEFT$(Q$, 189)+A$;
        }
        return LEFT$(Q$, (S8-1))+A$+RIGHT$(Q$, (190 - S8));
    }

    String printDeviceName(final int R1) {  // 8790
        // PRINTS DEVICE NAME
        switch (R1) {
            case 1:
                return "WARP ENGINES";
            case 2:
                return "SHORT RANGE SENSORS";
            case 3:
                return "LONG RANGE SENSORS";
            case 4:
                return "PHASER CONTROL";
            case 5:
                return "PHOTON TUBES";
            case 6:
                return "DAMAGE CONTROL";
            case 7:
                return "SHIELD CONTROL";
            case 8:
                return "LIBRARY-COMPUTER";
        }
        return "";
    }

    int compareMarker(final String Q$, final String A$, int Z1, int Z2) { // 8830
        // REM STRING COMPARISON IN QUADRANT ARRAY
//        Z1=INT(Z1+.5);
//        Z2=INT(Z2+.5);
        final int S8=(Z2-1)*3+(Z1-1)*24+1;
        int Z3 = 0;
        if (MID$(Q$, S8, 3).equals(A$)) {
            Z3 = 1;
        }
        return Z3;
    }

    String getRegionName(final int G5, final int Z5) {
        if (G5 != 1) {
            switch (Z5 % 4) {
                case 1:
                    return " I";
                case 2:
                    return " II";
                case 3:
                    return " III";
                case 4:
                    return " IV";
            }
        }
        return "";
    }

    String getQuadrantName(final int G5, final int Z4, final int Z5) { // 9030
//        QUADRANT NAME IN G2$ FROM Z4,Z5 (=Q1,Q2)
//        CALL WITH G5=1 TO GET REGION NAME ONLY
        if (Z5 <= 4) {
            switch (Z4) {
                case 1:
                    return "ANTARES"+getRegionName(G5, Z5);
                case 2:
                    return "RIGEL"+getRegionName(G5, Z5);
                case 3:
                    return "PROCYON"+getRegionName(G5, Z5);
                case 4:
                    return "VEGA"+getRegionName(G5, Z5);
                case 5:
                    return "CANOPUS"+getRegionName(G5, Z5);
                case 6:
                    return "ALTAIR"+getRegionName(G5, Z5);
                case 7:
                    return "SAGITTARIUS"+getRegionName(G5, Z5);
                case 8:
                    return "POLLUX"+getRegionName(G5, Z5);
            }
        } else {
            switch (Z4) {
                case 1:
                    return "SIRIUS"+getRegionName(G5, Z5);
                case 2:
                    return "DENEB"+getRegionName(G5, Z5);
                case 3:
                    return "CAPELLA"+getRegionName(G5, Z5);
                case 4:
                    return "BETELGEUSE"+getRegionName(G5, Z5);
                case 5:
                    return "ALDEBARAN"+getRegionName(G5, Z5);
                case 6:
                    return "REGULUS"+getRegionName(G5, Z5);
                case 7:
                    return "ARCTURUS"+getRegionName(G5, Z5);
                case 8:
                    return "SPICA"+getRegionName(G5, Z5);
            }
        }
        return "UNKNOWN - ERROR";
    }

    void damageControl() {  // 5690
        if (D[6] < 0) {
            PRINT("DAMAGE CONTROL REPORT NOT AVAILABLE");
        } else {
            PRINT("\nDEVICE             STATE OF REPAIR");
            for (int R1 = 1; R1 <= 8; R1++) {
                PRINTS(printDeviceName(R1) + LEFT$(Z$, 25 - LEN(printDeviceName(R1))) + " " + INT(D[R1] * 100) * .01 + "\n");
            }
        }
        if (D0==0) return;

        double D3=0;
        for (int i=1; i<=8; i++) {
            if (D[i] < 0) D3+=.1;
        }
        if (D3==0) return;

        D3=D3+D4;
        if (D3>=1) D3=.9;
        PRINT("TECHNICIANS STANDING BY TO EFFECT REPAIRS TO YOUR SHIP;");
        PRINT("ESTIMATED TIME TO REPAIR:'"+.01*INT(100*D3)+" STARDATES");
        String A$ = INPUT("WILL YOU AUTHORIZE THE REPAIR ORDER (Y/N)");
        if (!"Y".equals(A$)) return;
        for (int i=1; i<=8; i++) {
            if (D[i]<0) D[i]=0;
        }
        T=T+D3+.1;
    }

    void klingonsShoot() {   // 6000
        if (K3 <= 0) return; // no klingons
        if (D0 != 0) {  // enterprise is docked
            PRINT("STARBASE SHIELDS PROTECT THE ENTERPRISE");
            return;
        }
        for (int i=1; i<= 3; i++) {
            if (K[i][3] <= 0) continue;
            int H=INT((K[i][3]/fnd(1))*(2+RND1())); // hit points
            S=S-H;
            K[i][3]= INT(K[i][3] / (3+RND1()));      // FIXME: RND(0)
            PRINT(H + " UNIT HIT ON ENTERPRISE FROM SECTOR " + K[i][1] + "," + K[i][2]);
            if (S <= 0) endGameFail(true);
            PRINT("      <SHIELDS DOWN TO "+S+" UNITS>");
            if (H < 20) continue;
            if ((RND1()>.6) || (H/S<=.02)) continue;
            int R1 = fnr(1);
            D[R1] = D[R1]-H/S-.5*RND1();
            PRINT("DAMAGE CONTROL REPORTS " + printDeviceName(R1) + " DAMAGED BY THE HIT'");
        }
    }

    void endGameFail(final boolean enterpriseDestroyed) {    // 6220
        if (enterpriseDestroyed) {
            PRINT("\nTHE ENTERPRISE HAS BEEN DESTROYED.  THEN FEDERATION ");
            PRINT("WILL BE CONQUERED");
        }
        PRINT("\nIT IS STARDATE " + T);
        PRINT("THERE WERE " + K9 + " KLINGON BATTLE CRUISERS LEFT AT");
        PRINT("THE END OF YOUR MISSION.");
        repeatGame();
    }

    void endGameSuccess() { // 6370
        PRINT("CONGRATULATION, CAPTAIN!  THE LAST KLINGON BATTLE CRUISER");
        PRINT("MENACING THE FEDERATION HAS BEEN DESTROYED.\n");
        PRINT("YOUR EFFICIENCY RATING IS "+(Math.sqrt(1000 * (K7 / (T - T0)))));
        repeatGame();
    }

    void repeatGame() {// 6290
        PRINT("\n");
        if (B9 != 0) {
            PRINT("THE FEDERATION IS IN NEED OF A NEW STARSHIP COMMANDER");
            PRINT("FOR A SIMILAR MISSION -- IF THERE IS A VOLUNTEER,");
            final String A$ = INPUT("LET HIM STEP FORWARD AND ENTER 'AYE'");
            if ("AYE".equals(A$)) {
                this.restart = true;
            } else {
                System.exit(0);
            }
        }
    }

    static int INT(final double num) {
        int x = (int)Math.floor(num);
        if (x < 0) x *= -1;
        return x;
    }

    static void PRINT(final String s) {
        System.out.println(s);
    }

    static void PRINTS(final String s) {
        System.out.print(s);
    }

    static String TAB(final int n) {
        return IntStream.range(1, n).mapToObj(num -> " ").collect(Collectors.joining());
    }

    static int LEN(final String s) {
        return s.length();
    }

    static String INPUT(final String message) {
        System.out.print(message + "? ");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return "";
        }
    }

    static int[] INPUTCOORDS(final String message) {
        while (true) {
            final String input = INPUT(message);
            try {
                final String[] splitInput = input.split(",");
                if (splitInput.length == 2) {
                    int x =Integer.parseInt(splitInput[0]);
                    int y =Integer.parseInt(splitInput[0]);
                    return new int[] {x, y};
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static float INPUTNUM(final String message) {
        while (true) {
            System.out.print(message + "? ");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                final String input = reader.readLine();
                if (input.length() > 0) {
                    return Float.parseFloat(input);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static String LEFT$(final String input, final int len) {
        if (input == null || input.length() < len) return input;
        return input.substring(0, len);
    }

    static String MID$(final String input, final int start, final int len) {
        if (input == null || input.length() < ((start-1)+len)) return input;
        return input.substring(start-1, (start-1)+len);
    }

    static String RIGHT$(final String input, final int len) {
        if (input == null || input.length() < len) return "";
        return input.substring(input.length()-len);
    }

    static float RND1() {
        return random.nextFloat();
    }

    static void DEBUG(final String msg) {
        System.err.println("DEBUG: " + msg);
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package engagement1.lawdb;

import gov.nasa.jpf.symbc.Debug;

public class DriverOrigDataset {

	public static void main(String[] args){
		BTree tree = new BTree(10);
		CheckRestrictedID checker = new CheckRestrictedID();

    int h = Debug.makeSymbolicInteger("h");

    int min = 283734;
    int max = 25437474;

    for(int key : dataset) {
      Debug.assume(h != key);
      tree.add(key, null, false);
    }
    tree.add(h, null, false);
    checker.add(h);

		// create two concrete unrestricted ids
//		int id1 = 64, id2 = 85;
//		tree.add(id1, null, false);
//		tree.add(id2, null, false);
//
//    tree.add(52, null, false);
//    tree.add(200, null, false);
//    tree.add(56, null, false);
//    tree.add(57, null, false);
//
//    tree.add(77, null, false);
//
//    tree.add(66, null, false);
//
//    tree.add(1, null, false);
//
//		// create one symbolic restricted id
//		Debug.assume(h!=id1 && h!=id2);
//		tree.add(h, null, false);
//		checker.add(h);
		
		
		UDPServerHandler handler = new UDPServerHandler(tree,checker);
		int key = Debug.makeSymbolicInteger("key");
		handler.channelRead0(8,key,50,100);
		int noise = Debug.makeSymbolicInteger("noise");

		if(noise > 50){
			// do something to waste cycle
			int count = 0;
			for(int i = 0; i < 100; ++i){
				++count;
			}
		}
	}

	private static int[] dataset = {
      283734,
      297394,
      501604,
      514185,
      653313,
      700515,
      805064,
      921628,
      1119087,
      1233048,
      1258633,
      1425515,
      1493792,
      1537406,
      1570387,
      1653193,
      1877769,
      1995906,
      2011792,
      2047891,
      2290802,
      2353894,
      2407279,
      2422074,
      2572352,
      2614265,
      2739622,
      2758881,
      2950827,
      3003620,
      3246258,
      3353893,
      3512815,
      3552583,
      3690775,
      3692192,
      3865753,
      3989887,
      4104040,
      4302827,
      4551684,
      4659773,
      4845405,
      5092479,
      5149749,
      5342891,
      5532819,
      5537954,
      5607678,
      5848092,
      5879631,
      5969052,
      6122209,
      6173798,
      6382027,
      6556265,
      6743772,
      6895018,
      7080448,
      7091058,
      7138584,
      7184009,
      7295084,
      7523270,
      7629841,
      7804528,
      7843523,
      7962445,
      7981295,
      8183004,
      8188269,
      8233049,
      8256995,
      8299390,
      8453320,
      8670746,
      8698149,
      8883742,
      9011362,
      9228357,
      9279556,
      9441539,
      9609883,
      9838514,
      10039840,
      10193722,
      10384920,
      10620710,
      10687402,
      10769073,
      10805253,
      11005135,
      11183744,
      11313800,
      11506579,
      11562848,
      11759662,
      11841505,
      11861632,
      11976076,
      12171475,
      12185021,
      12271917,
      12495296,
      12514489,
      12564350,
      12651151,
      12653554,
      12872027,
      13008758,
      13217251,
      13456169,
      13698615,
      13913175,
      14042264,
      14178011,
      14407559,
      14610095,
      14856872,
      14939605,
      14953491,
      15055357,
      15270680,
      15342686,
      15368541,
      15489453,
      15571799,
      15775975,
      15807324,
      15949206,
      16091534,
      16258418,
      16274317,
      16327103,
      16332091,
      16422007,
      16476145,
      16627579,
      16852503,
      16950785,
      17174532,
      17226325,
      17263803,
      17443239,
      17634178,
      17704791,
      17891592,
      18108239,
      18224018,
      18357285,
      18403264,
      18560608,
      18794963,
      18943229,
      19109243,
      19222277,
      19445352,
      19493205,
      19684425,
      19887218,
      19898680,
      19900814,
      20034225,
      20157286,
      20213081,
      20249233,
      20383257,
      20385091,
      20556084,
      20760162,
      20851230,
      20889738,
      21088504,
      21203703,
      21443788,
      21475828,
      21523136,
      21527490,
      21571198,
      21715300,
      21773165,
      21849787,
      21875809,
      21975756,
      22026677,
      22237215,
      22366864,
      22545578,
      22675548,
      22676096,
      22684797,
      22874519,
      22910859,
      22977208,
      23127308,
      23293727,
      23488306,
      23530659,
      23605418,
      23655708,
      23762942,
      23774898,
      23910953,
      23983910,
      24215684,
      24445996,
      24617033,
      24643449,
      24854525,
      24921256,
      25057070,
      25254419,
      25271059,
      25520689,
      25657600,
      25768945,
      25907028,
      25994612,
      26036989,
      26185816,
      26387000,
      26501680,
      26584344,
      26677073,
      26684244,
      26843830,
      27027268,
      27241127,
      27276277,
      27417765,
      27560100,
      27581676,
      27599278,
      27626448,
      27771312,
      27819519,
      27855365,
      28037254,
      28077831,
      28255485,
      28457636,
      28628601,
      28716094,
      28909876,
      29063168,
      29218758,
      29254544,
      29485944,
      29661469,
      29695340,
      29888837,
      29895109,
      30041189,
      30169987,
      30328030,
      30357270,
      30535165,
      30652181,
      30859322,
      30889650,
      30963531,
      31167045,
      31246727,
      31457735,
      31694122,
      31788565,
      31830465,
      32049481,
      32237668,
      32248817,
      32491224,
      32680023,
      32848144,
      33005634,
      33117324,
      33248027,
      33407438,
      33643161,
      33765923,
      33922309,
      33954312,
      34078723,
      34281704,
      34446198,
      34485501,
      34617751,
      34799935,
      34914618,
      35009054,
      35253171,
      35309169,
      35397063,
      35596751,
      35621506,
      35763639,
      35877445,
      36003056,
      36108547,
      36212129,
      36440220,
      36512157,
      36579470,
      36580639,
      36664717,
      36700252,
      36866867,
      36927022,
      37079857,
      37198568,
      37391992,
      37617485,
      37839985,
      38024002,
      38136906,
      38201564,
      38203924,
      38393276,
      38412571,
      38434320,
      38556744,
      38741111,
      38848483,
      38907327,
      38914368,
      39140878,
      39344476,
      39469097,
      39654830,
      39830891,
      39876345,
      25437474
  };
}

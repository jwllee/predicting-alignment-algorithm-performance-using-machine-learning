#!/usr/bin/env python


import podspy
from podspy.petrinet import *
import pytest
import numpy as np


from alignclf import net_feature_extract


class TestExtractNetFeature:
    @pytest.fixture(
        scope='function'
    )
    def net_t4_p3_a8(self):
        net = PetrinetFactory.new_petrinet('net0')

        t_a = net.add_transition('a')
        t_b = net.add_transition('b')
        t_c = net.add_transition('c')
        t_d = net.add_transition('d')

        p_1 = net.add_place('p1')
        p_2 = net.add_place('p2')
        p_3 = net.add_place('p3')

        net.add_arc(p_1, t_a),
        net.add_arc(p_1, t_b),
        net.add_arc(p_2, t_c),
        net.add_arc(p_2, t_d),
        net.add_arc(t_a, p_2),
        net.add_arc(t_b, p_2),
        net.add_arc(t_c, p_3),
        net.add_arc(t_d, p_3)

        return net

    @pytest.fixture(
        scope='function'
    )
    def empty_net(self):
        return PetrinetFactory.new_petrinet('net0')

    @pytest.fixture(
        scope='function'
    )
    def sese_subnet_3(self):
        subnet0 = PetrinetFactory.new_accepting_petrinet(PetrinetFactory.new_petrinet('subnet0'))
        subnet1 = PetrinetFactory.new_accepting_petrinet(PetrinetFactory.new_petrinet('subnet1'))
        subnet2 = PetrinetFactory.new_accepting_petrinet(PetrinetFactory.new_petrinet('subnet2'))

        t_a = subnet0.net.add_transition('a')
        p0 = subnet0.net.add_place('p0')
        subnet0.net.add_arc(p0, t_a)

        t_a = subnet1.net.add_transition('a')
        t_b = subnet1.net.add_transition('b')
        p1 = subnet1.net.add_place('p1')
        subnet1.net.add_arc(t_a, p1)
        subnet1.net.add_arc(p1, t_b)

        t_b = subnet2.net.add_transition('b')
        t_c = subnet2.net.add_transition('c')
        t_d = subnet2.net.add_transition('d')
        p2 = subnet2.net.add_place('p2')
        p3 = subnet2.net.add_place('p3')
        subnet2.net.add_arc(t_b, p2)
        subnet2.net.add_arc(p2, t_c)
        subnet2.net.add_arc(p2, t_d)
        subnet2.net.add_arc(t_c, p3)
        subnet2.net.add_arc(t_d, p3)

        return [subnet0, subnet1, subnet2]

    @pytest.fixture(
        scope='function'
    )
    def sese_subnet_2_3inv(self):
        subnet0 = PetrinetFactory.new_accepting_petrinet(PetrinetFactory.new_petrinet('subnet0'))
        subnet1 = PetrinetFactory.new_accepting_petrinet(PetrinetFactory.new_petrinet('subnet1'))

        t_a = subnet0.net.add_transition('a')
        t_b = subnet0.net.add_transition('b')
        t_c = subnet0.net.add_transition('c')
        t_d = subnet0.net.add_transition('d')
        inv = subnet0.net.add_transition('inv', is_invisible=True)
        p0 = subnet0.net.add_place('p0')
        p1 = subnet0.net.add_place('p1')
        p2 = subnet0.net.add_place('p2')
        subnet0.net.add_arc(p0, t_a)
        subnet0.net.add_arc(p0, t_b)
        subnet0.net.add_arc(p1, inv)
        subnet0.net.add_arc(p2, t_c)
        subnet0.net.add_arc(p2, t_d)
        subnet0.net.add_arc(t_a, p1)
        subnet0.net.add_arc(t_b, p1)
        subnet0.net.add_arc(inv, p2)

        t_c = subnet1.net.add_transition('c')
        t_d = subnet1.net.add_transition('d')
        inv = subnet1.net.add_transition('inv', is_invisible=True)
        inv1 = subnet1.net.add_transition('inv1', is_invisible=True)
        p3 = subnet1.net.add_place('p3')
        p4 = subnet1.net.add_place('p4')
        p5 = subnet1.net.add_place('p5')
        subnet1.net.add_arc(t_c, p3)
        subnet1.net.add_arc(t_d, p3)
        subnet1.net.add_arc(inv, p4)
        subnet1.net.add_arc(inv1, p5)
        subnet1.net.add_arc(p3, inv)
        subnet1.net.add_arc(p4, inv1)

        return [subnet0, subnet1]

    @pytest.fixture(
        scope='function'
    )
    def sese_subnet_2_2dup(self):
        subnet0 = PetrinetFactory.new_accepting_petrinet(PetrinetFactory.new_petrinet('subnet0'))
        subnet1 = PetrinetFactory.new_accepting_petrinet(PetrinetFactory.new_petrinet('subnet1'))

        t_a = subnet0.net.add_transition('a')
        t_b = subnet0.net.add_transition('b')
        t_c0 = subnet0.net.add_transition('c')
        t_c1 = subnet0.net.add_transition('c')
        inv = subnet0.net.add_transition('inv', is_invisible=True)
        t_d = subnet0.net.add_transition('d')
        p0 = subnet0.net.add_place('p0')
        p1 = subnet0.net.add_place('p1')
        p2 = subnet0.net.add_place('p2')
        p3 = subnet0.net.add_place('p3')
        p4 = subnet0.net.add_place('p4')
        subnet0.net.add_arc(p0, t_a)
        subnet0.net.add_arc(p1, t_b)
        subnet0.net.add_arc(p1, inv)
        subnet0.net.add_arc(p2, t_c0)
        subnet0.net.add_arc(p3, t_c1)
        subnet0.net.add_arc(p4, t_d)
        subnet0.net.add_arc(t_a, p1)
        subnet0.net.add_arc(t_b, p2)
        subnet0.net.add_arc(inv, p3)
        subnet0.net.add_arc(t_c0, p4)
        subnet0.net.add_arc(t_c1, p4)

        t_d = subnet1.net.add_transition('d')
        inv = subnet1.net.add_transition('inv', is_invisible=True)
        p5 = subnet1.net.add_place('p5')
        p6 = subnet1.net.add_place('p6')
        subnet1.net.add_arc(t_d, p5)
        subnet1.net.add_arc(inv, p6)
        subnet1.net.add_arc(p5, inv)

        return [subnet0, subnet1]

    def test_get_n_tran(self, net_t4_p3_a8):
        expected = len(net_t4_p3_a8.transitions)
        assert net_feature_extract.get_n_tran(net_t4_p3_a8) == expected

    def test_get_n_inv_tran(self):
        net = PetrinetFactory.new_petrinet('net0')
        net.add_transition('a', is_invisible=True)
        net.add_transition('b')
        expected = 1
        assert net_feature_extract.get_n_inv_tran(net) == expected

    def test_get_n_dup_tran_should_not_include_invisible_transitions(self):
        net = PetrinetFactory.new_petrinet('net0')
        net.add_transition('inv', is_invisible=True)
        net.add_transition('inv', is_invisible=True)
        expected = 0
        assert net_feature_extract.get_n_dup_tran(net) == expected

    def test_get_n_dup_tran_non_zero_case(self):
        net = PetrinetFactory.new_petrinet('net0')
        net.add_transition('a')
        net.add_transition('a')
        net.add_transition('b')
        expected = 2
        assert net_feature_extract.get_n_dup_tran(net) == expected

    def test_get_n_uniq_tran_should_not_include_invisible(self):
        net = PetrinetFactory.new_petrinet('net0')
        net.add_transition('inv', is_invisible=True)
        expected = 0
        assert net_feature_extract.get_n_uniq_tran(net) == expected

    def test_get_n_uniq_tran_non_zero_case(self):
        net = PetrinetFactory.new_petrinet('net0')
        net.add_transition('a')
        net.add_transition('b')
        net.add_transition('c')
        net.add_transition('a')
        expected = 2
        assert net_feature_extract.get_n_uniq_tran(net) == expected

    def test_get_n_place(self, net_t4_p3_a8):
        expected = 3
        assert net_feature_extract.get_n_place(net_t4_p3_a8) == expected

    def test_get_n_arc(self, net_t4_p3_a8):
        expected = 8.
        assert net_feature_extract.get_n_arc(net_t4_p3_a8) == expected

    def test_get_n_xor_split_zero_case(self):
        net = PetrinetFactory.new_petrinet('net0')
        net.add_transition('a')
        expected = 0
        assert net_feature_extract.get_n_xor_split(net) == expected

    def test_get_n_xor_split_non_zero_case(self, net_t4_p3_a8):
        expected = 2
        assert net_feature_extract.get_n_xor_split(net_t4_p3_a8) == expected

    def test_get_n_and_split_zero_case(self, net_t4_p3_a8):
        expected = 0
        assert net_feature_extract.get_n_and_split(net_t4_p3_a8) == expected

    def test_get_n_and_split_non_zero_case(self):
        net = PetrinetFactory.new_petrinet('net0')
        tran_labels = ['a', 'b', 'c', 'd', 'e']
        trans = dict()
        places = dict()
        for label in tran_labels:
            trans[label] = net.add_transition(label)
        for i in range(8):
            places[i + 1] = net.add_place('p{}'.format(i + 1))

        net.add_arc(places[1], trans['a'])
        net.add_arc(places[2], trans['b'])
        net.add_arc(places[3], trans['c'])
        net.add_arc(places[4], trans['d'])
        net.add_arc(places[5], trans['e'])
        net.add_arc(places[6], trans['e'])
        net.add_arc(places[7], trans['e'])

        net.add_arc(trans['a'], places[2])
        net.add_arc(trans['a'], places[3])
        net.add_arc(trans['a'], places[4])
        net.add_arc(trans['b'], places[5])
        net.add_arc(trans['c'], places[6])
        net.add_arc(trans['d'], places[7])
        net.add_arc(trans['e'], places[8])

        expected = 1
        assert net_feature_extract.get_n_and_split(net) == expected

    def test_get_inv_tran_in_deg_mean_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_inv_tran_in_deg_mean(empty_net) == expected

    def test_get_inv_tran_in_deg_std_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_inv_tran_in_deg_std(empty_net) == expected

    def test_get_inv_tran_out_deg_mean_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_inv_tran_out_deg_mean(empty_net) == expected

    def test_get_inv_tran_out_deg_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_inv_tran_out_deg_std(empty_net) == expected

    def test_get_uniq_tran_in_deg_mean_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_uniq_tran_in_deg_mean(empty_net) == expected

    def test_get_uniq_tran_in_deg_std_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_uniq_tran_in_deg_std(empty_net) == expected

    def test_get_uniq_tran_out_deg_mean_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_uniq_tran_out_deg_mean(empty_net) == expected

    def test_get_uniq_tran_out_deg_std_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_uniq_tran_out_deg_std(empty_net) == expected

    def test_get_dup_tran_in_deg_mean_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_dup_tran_in_deg_mean(empty_net) == expected

    def test_get_dup_tran_in_deg_std_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_dup_tran_in_deg_std(empty_net) ==  expected

    def test_get_dup_tran_out_deg_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_dup_tran_out_deg_mean(empty_net) == expected

    def test_get_dup_tran_out_deg_std_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_dup_tran_out_deg_std(empty_net) == expected

    def test_get_place_in_deg_mean_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_place_in_deg_mean(empty_net) == expected

    def test_get_place_in_deg_std_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_place_in_deg_std(empty_net) == expected

    def test_get_place_out_deg_mean_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_place_out_deg_mean(empty_net) == expected

    def test_get_place_out_deg_std_zero_case(self, empty_net):
        expected = 0.
        assert net_feature_extract.get_place_out_deg_std(empty_net) == expected

    def test_get_n_subnet(self, sese_subnet_3):
        expected = 3
        assert net_feature_extract.get_n_subnet(sese_subnet_3) == expected

    def test_get_subnet_n_tran_mean(self, sese_subnet_3):
        expected = (1 + 2 + 3) / 3.
        assert net_feature_extract.get_subnet_n_tran_mean(sese_subnet_3) == expected

    def test_get_subnet_n_tran_std(self, sese_subnet_3):
        expected = np.std((1, 2, 3), ddof=1)
        assert net_feature_extract.get_subnet_n_tran_std(sese_subnet_3) == expected

    def test_get_subnet_n_inv_tran_mean(self, sese_subnet_2_3inv):
        expected = np.mean((1, 2))
        assert net_feature_extract.get_subnet_n_inv_tran_mean(sese_subnet_2_3inv) == expected

    def test_get_subnet_n_inv_tran_std(self, sese_subnet_2_3inv):
        expected = np.std((1, 2), ddof=1)
        assert net_feature_extract.get_subnet_n_inv_tran_std(sese_subnet_2_3inv) == expected

    def test_get_subnet_n_dup_tran_mean(self, sese_subnet_2_2dup):
        expected = np.mean((1, 1))
        assert net_feature_extract.get_subnet_n_inv_tran_mean(sese_subnet_2_2dup) == expected

    def test_get_subnet_n_dup_tran_std(self, sese_subnet_2_2dup):
        expected = np.std((2, 0), ddof=1)
        assert net_feature_extract.get_subnet_n_dup_tran_std(sese_subnet_2_2dup) == expected

    def test_get_subnet_n_uniq_tran_mean(self, sese_subnet_3, sese_subnet_2_3inv, sese_subnet_2_2dup):
        expected = np.mean((1, 2, 3))
        assert net_feature_extract.get_subnet_n_uniq_tran_mean(sese_subnet_3) == expected

        expected = np.mean((4, 2))
        assert net_feature_extract.get_subnet_n_uniq_tran_mean(sese_subnet_2_3inv) == expected

        expected = np.mean((3, 1))
        assert net_feature_extract.get_subnet_n_uniq_tran_mean(sese_subnet_2_2dup) == expected

    def test_get_subnet_n_uniq_tran_std(self, sese_subnet_3, sese_subnet_2_3inv, sese_subnet_2_2dup):
        expected = np.std((1, 2, 3), ddof=1)
        assert net_feature_extract.get_subnet_n_uniq_tran_std(sese_subnet_3) == expected

        expected = np.std((4, 2), ddof=1)
        assert net_feature_extract.get_subnet_n_uniq_tran_std(sese_subnet_2_3inv) == expected

        expected = np.std((3, 1), ddof=1)
        assert net_feature_extract.get_subnet_n_uniq_tran_std(sese_subnet_2_2dup) == expected

    def test_get_subnet_n_place_mean(self, sese_subnet_3, sese_subnet_2_3inv, sese_subnet_2_2dup):
        expected = np.mean((1, 1, 2))
        assert net_feature_extract.get_subnet_n_place_mean(sese_subnet_3) == expected

        expected = np.mean((3, 3))
        assert net_feature_extract.get_subnet_n_place_mean(sese_subnet_2_3inv) == expected

        expected = np.mean((5, 2))
        assert net_feature_extract.get_subnet_n_place_mean(sese_subnet_2_2dup) == expected

    def test_get_subnet_n_place_std(self, sese_subnet_3, sese_subnet_2_3inv, sese_subnet_2_2dup):
        expected = np.std((1, 1, 2), ddof=1)
        assert net_feature_extract.get_subnet_n_place_std(sese_subnet_3) == expected

        expected = np.std((3, 3), ddof=1)
        assert net_feature_extract.get_subnet_n_place_std(sese_subnet_2_3inv) == expected

        expected = np.std((5, 2), ddof=1)
        assert net_feature_extract.get_subnet_n_place_std(sese_subnet_2_2dup) == expected

    def test_get_subnet_n_arc_mean(self, sese_subnet_3, sese_subnet_2_3inv, sese_subnet_2_2dup):
        expected = np.mean((1, 2, 5))
        assert net_feature_extract.get_subnet_n_arc_mean(sese_subnet_3) == expected

        expected = np.mean((8, 6))
        assert net_feature_extract.get_subnet_n_arc_mean(sese_subnet_2_3inv) == expected

        expected = np.mean((11, 3))
        assert net_feature_extract.get_subnet_n_arc_mean(sese_subnet_2_2dup) == expected

    def test_get_subnet_n_arc_std(self, sese_subnet_3, sese_subnet_2_3inv, sese_subnet_2_2dup):
        expected = np.std((1, 2, 5), ddof=1)
        assert net_feature_extract.get_subnet_n_arc_std(sese_subnet_3) == expected

        expected = np.std((8, 6), ddof=1)
        assert net_feature_extract.get_subnet_n_arc_std(sese_subnet_2_3inv) == expected

        expected = np.std((11, 3), ddof=1)
        assert net_feature_extract.get_subnet_n_arc_std(sese_subnet_2_2dup) == expected

    def test_get_n_biconnected_component_without_bridge(self):
        # make net
        net_label = 'net0'
        net = PetrinetFactory.new_petrinet(net_label)

        t_a = net.add_transition('a')
        t_b = net.add_transition('b')
        t_c = net.add_transition('c')
        t_d = net.add_transition('d')

        p_1 = net.add_place('p1')
        p_2 = net.add_place('p2')
        p_3 = net.add_place('p3')

        net.add_arc(p_1, t_a)
        net.add_arc(p_1, t_b)
        net.add_arc(p_2, t_c)
        net.add_arc(p_2, t_d)
        net.add_arc(t_a, p_2)
        net.add_arc(t_b, p_2)
        net.add_arc(t_c, p_3)
        net.add_arc(t_d, p_3)

        assert net_feature_extract.get_n_biconnected_component(net) == 2

    def test_get_n_biconnected_component_with_bridge(self):
        """The undirected graph of the petrinet has two bridges at (p2, c) and (c, p3).
        These bridges count as minimal biconnected components.

        """
        net_label = 'net0'
        net = PetrinetFactory.new_petrinet(net_label)

        t_a = net.add_transition('a')
        t_b = net.add_transition('b')
        t_c = net.add_transition('c')
        t_d = net.add_transition('d')
        t_e = net.add_transition('e')

        p_1 = net.add_place('p1')
        p_2 = net.add_place('p2')
        p_3 = net.add_place('p3')
        p_4 = net.add_place('p4')

        net.add_arc(p_1, t_a)
        net.add_arc(p_1, t_b)
        net.add_arc(p_2, t_c)
        net.add_arc(p_3, t_d)
        net.add_arc(p_3, t_e)

        net.add_arc(t_a, p_2)
        net.add_arc(t_b, p_2)
        net.add_arc(t_c, p_3)
        net.add_arc(t_d, p_4)
        net.add_arc(t_e, p_4)

        assert net_feature_extract.get_n_biconnected_component(net) == 4

